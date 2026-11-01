package com.ketowatch.core

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.errors.AnthropicIoException
import com.anthropic.errors.AnthropicServiceException
import com.anthropic.errors.PermissionDeniedException
import com.anthropic.errors.RateLimitException
import com.anthropic.errors.UnauthorizedException
import com.anthropic.models.messages.Base64ImageSource
import com.anthropic.models.messages.CacheControlEphemeral
import com.anthropic.models.messages.ContentBlockParam
import com.anthropic.models.messages.ImageBlockParam
import com.anthropic.models.messages.JsonOutputFormat
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.OutputConfig
import com.anthropic.models.messages.StopReason
import com.anthropic.models.messages.TextBlockParam
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.Base64

/** Thrown with a message that is safe and friendly to show to the user. */
class RecognitionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Which Claude model does the photo analysis. */
enum class RecognitionModel(val id: String, val label: String, val supportsFallbacks: Boolean) {
    BEST("claude-opus-5-5", "Best accuracy (Claude Opus 5.5)", true),
    FAST("claude-haiku-5-5", "Faster & cheaper (Claude Haiku 5.5)", false),
}

/**
 * Identifies the food in a photo and estimates its nutrition using Claude's vision,
 * primed with the Ugandan food reference table so local dishes (matooke, luwombo,
 * rolex, malakwang, …) and Ugandan shop products are recognised and portioned
 * the way they are actually served.
 */
class FoodRecognizer(
    apiKey: String,
    private val model: RecognitionModel = RecognitionModel.BEST,
) {
    private val client: AnthropicClient = AnthropicOkHttpClient.builder()
        .apiKey(apiKey)
        .timeout(Duration.ofSeconds(120))
        .maxRetries(2)
        .build()

    /** [jpeg] should already be downscaled (≈1024 px on the long side keeps uploads small). */
    fun analyze(jpeg: ByteArray, userHint: String? = null): MealAnalysis {
        val params = buildRequest(jpeg, userHint)
        val response = try {
            client.messages().create(params)
        } catch (e: UnauthorizedException) {
            throw RecognitionException("Your Claude API key was rejected. Check it in Settings.", e)
        } catch (e: PermissionDeniedException) {
            throw RecognitionException("This API key isn't allowed to use the selected model.", e)
        } catch (e: RateLimitException) {
            throw RecognitionException("Too many scans right now. Wait a minute and try again.", e)
        } catch (e: AnthropicServiceException) {
            throw RecognitionException("The food recognition service had a problem (${e.statusCode()}). Please try again.", e)
        } catch (e: AnthropicIoException) {
            throw RecognitionException("No internet connection. You can still add the meal by picking it from the food list.", e)
        }

        if (response.stopReason().orElse(null) == StopReason.REFUSAL) {
            throw RecognitionException("This photo couldn't be analysed. Try another photo or pick the food from the list.")
        }
        val text = response.content().mapNotNull { block -> block.text().orElse(null)?.text() }.joinToString("")
        if (text.isBlank()) throw RecognitionException("No result came back. Please try again.")
        return parse(text)
    }

    internal fun buildRequest(jpeg: ByteArray, userHint: String?): MessageCreateParams {
        val image = ImageBlockParam.builder()
            .source(
                Base64ImageSource.builder()
                    .mediaType(Base64ImageSource.MediaType.IMAGE_JPEG)
                    .data(Base64.getEncoder().encodeToString(jpeg))
                    .build(),
            )
            .build()
        val instruction = buildString {
            append("Identify the food in this photo and estimate the nutrition of the portion shown.")
            if (!userHint.isNullOrBlank()) append(" The user says: \"").append(userHint.trim()).append("\".")
        }

        val builder = MessageCreateParams.builder()
            .model(model.id)
            .maxTokens(16000L)
            .systemOfTextBlockParams(
                listOf(
                    TextBlockParam.builder()
                        .text(SYSTEM_PROMPT)
                        .cacheControl(CacheControlEphemeral.builder().build())
                        .build(),
                ),
            )
            .outputConfig(
                OutputConfig.builder()
                    .effort(OutputConfig.Effort.MEDIUM)
                    .format(JsonOutputFormat.builder().schema(SCHEMA).build())
                    .build(),
            )
            .addUserMessageOfContentBlockParams(
                listOf(
                    ContentBlockParam.ofImage(image),
                    ContentBlockParam.ofText(TextBlockParam.builder().text(instruction).build()),
                ),
            )
        if (model.supportsFallbacks) {
            // If a safety classifier declines the request, let the API retry it on a suitable model.
            builder.putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
            builder.putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
        }
        return builder.build()
    }

    companion object {
        private val mapper = ObjectMapper()

        val SYSTEM_PROMPT: String = """
            You are the food-recognition engine of KETO WATCH, a calorie and keto tracker built for Uganda.
            Users photograph home-cooked Ugandan meals, restaurant and roadside food (rolex, kikomando, muchomo,
            chips kuku), market produce, and packaged products bought in Ugandan supermarkets and shops.

            How to work:
            - Name dishes the way Ugandans do. Prefer the local name in local_name (for example Matooke, Posho/Kawunga,
              Luwombo, Binyebwa, Malakwang, Katogo, Rolex, Nsenene, Dodo, Nakati, Entula, Gonja).
            - A Ugandan plate is often a "food" (matooke, posho, rice, cassava, sweet potato…) plus a "sauce"
              (groundnuts, beans, meat, fish, greens). List each component as a separate item.
            - For packaged products, read the label or brand if visible and use typical nutrition for that product.
            - Estimate grams from visual cues: plate size (Ugandan dinner plates are about 25 cm), cups, hands,
              packaging. Calculate nutrition for the amount actually shown, not a standard serving.
            - Use the reference table below as your baseline for Ugandan foods and scale it to the portion you see.
              For foods not in the table, use standard food-composition values.
            - Account for cooking oil in fried food and for sugar in tea, juice and porridge.
            - carbs_g is total carbohydrate; fiber_g is dietary fibre (net carbs are computed from these).
            - If the photo does not show food or drink, set is_food to false, give an empty items list and explain in notes.
            - confidence: "high" if the dish is clear, "medium" if partly hidden or ambiguous, "low" if guessing.
            - notes: one short, friendly sentence a user would find useful (for example what you assumed about the portion).

            Ugandan food reference (per serving):
            ${UgandaFoodDatabase.asPromptReference()}
        """.trimIndent()

        private fun num() = mapOf("type" to "number")
        private fun str() = mapOf("type" to "string")

        val SCHEMA: JsonOutputFormat.Schema = JsonOutputFormat.Schema.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty(
                "properties",
                JsonValue.from(
                    mapOf(
                        "is_food" to mapOf("type" to "boolean"),
                        "dish_name" to str(),
                        "items" to mapOf(
                            "type" to "array",
                            "items" to mapOf(
                                "type" to "object",
                                "properties" to mapOf(
                                    "name" to str(),
                                    "local_name" to str(),
                                    "portion" to str(),
                                    "grams" to num(),
                                    "calories" to num(),
                                    "carbs_g" to num(),
                                    "fiber_g" to num(),
                                    "protein_g" to num(),
                                    "fat_g" to num(),
                                ),
                                "required" to listOf("name", "local_name", "portion", "grams", "calories", "carbs_g", "fiber_g", "protein_g", "fat_g"),
                                "additionalProperties" to false,
                            ),
                        ),
                        "confidence" to mapOf("type" to "string", "enum" to listOf("high", "medium", "low")),
                        "notes" to str(),
                    ),
                ),
            )
            .putAdditionalProperty("required", JsonValue.from(listOf("is_food", "dish_name", "items", "confidence", "notes")))
            .putAdditionalProperty("additionalProperties", JsonValue.from(false))
            .build()

        /** Parses the model's JSON. Tolerant of missing/negative numbers so one odd field never crashes the app. */
        fun parse(json: String): MealAnalysis {
            val root: JsonNode = try {
                mapper.readTree(json)
            } catch (e: Exception) {
                throw RecognitionException("The result couldn't be read. Please try again.", e)
            }
            fun JsonNode.d(field: String) = path(field).asDouble(0.0).coerceAtLeast(0.0)
            val items = root.path("items").map { n ->
                DetectedItem(
                    name = n.path("name").asText("Food"),
                    localName = n.path("local_name").asText(""),
                    portion = n.path("portion").asText(""),
                    grams = n.d("grams"),
                    nutrition = Nutrition(n.d("calories"), n.d("carbs_g"), n.d("fiber_g"), n.d("protein_g"), n.d("fat_g")),
                )
            }
            return MealAnalysis(
                isFood = root.path("is_food").asBoolean(items.isNotEmpty()),
                dishName = root.path("dish_name").asText("").ifBlank { items.firstOrNull()?.name ?: "Meal" },
                items = items,
                confidence = root.path("confidence").asText("medium"),
                notes = root.path("notes").asText(""),
            )
        }
    }
}
