package com.ketowatch.core

import kotlin.math.max
import kotlin.math.roundToInt

/** Nutrition for one portion of a food. All weights in grams, energy in kcal. */
data class Nutrition(
    val calories: Double,
    val carbs: Double,
    val fiber: Double,
    val protein: Double,
    val fat: Double,
) {
    /** Net carbs = total carbs minus fibre (never negative). This is what counts on keto. */
    val netCarbs: Double get() = max(0.0, carbs - fiber)

    operator fun plus(o: Nutrition) = Nutrition(
        calories + o.calories, carbs + o.carbs, fiber + o.fiber, protein + o.protein, fat + o.fat,
    )

    fun scaled(factor: Double) = Nutrition(
        calories * factor, carbs * factor, fiber * factor, protein * factor, fat * factor,
    )

    companion object {
        val ZERO = Nutrition(0.0, 0.0, 0.0, 0.0, 0.0)
    }
}

enum class FoodCategory(val label: String) {
    STAPLE("Staples"),
    SAUCE_STEW("Sauces & stews"),
    MEAT_FISH("Meat, fish & eggs"),
    VEGETABLE("Vegetables & greens"),
    STREET_FOOD("Street food & snacks"),
    FRUIT("Fruits"),
    DRINK("Drinks"),
    PACKAGED("Supermarket & shop items"),
}

/** A reference food from the built-in Ugandan database. */
data class FoodItem(
    val id: String,
    val name: String,
    val localNames: List<String>,
    val category: FoodCategory,
    val serving: String,
    val servingGrams: Int,
    val nutrition: Nutrition,
) {
    val keto: KetoRating get() = KetoRules.rate(nutrition)

    fun matches(query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        return name.lowercase().contains(q) || localNames.any { it.lowercase().contains(q) }
    }
}

/** One food component detected on the plate (e.g. "matooke" + "groundnut sauce"). */
data class DetectedItem(
    val name: String,
    val localName: String,
    val portion: String,
    val grams: Double,
    val nutrition: Nutrition,
)

data class MealAnalysis(
    val isFood: Boolean,
    val dishName: String,
    val items: List<DetectedItem>,
    val confidence: String,
    val notes: String,
) {
    val total: Nutrition get() = items.fold(Nutrition.ZERO) { acc, i -> acc + i.nutrition }
    val keto: KetoRating get() = KetoRules.rate(total)

    /** Scale the whole meal for "I ate half" / "I ate double" adjustments. */
    fun scaled(factor: Double) = copy(
        items = items.map { it.copy(grams = it.grams * factor, nutrition = it.nutrition.scaled(factor)) },
    )

    companion object {
        fun fromFood(food: FoodItem, servings: Double = 1.0) = MealAnalysis(
            isFood = true,
            dishName = food.name,
            items = listOf(
                DetectedItem(
                    name = food.name,
                    localName = food.localNames.firstOrNull().orEmpty(),
                    portion = food.serving,
                    grams = food.servingGrams.toDouble(),
                    nutrition = food.nutrition,
                ),
            ),
            confidence = "manual",
            notes = "",
        ).scaled(servings)
    }
}

fun Double.r0(): Int = roundToInt()
fun Double.r1(): String = ((this * 10).roundToInt() / 10.0).toString()
