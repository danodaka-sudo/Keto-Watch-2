package com.ketowatch.core

import kotlin.math.max
import kotlin.math.roundToInt

enum class KetoRating(val label: String, val emoji: String) {
    FRIENDLY("Keto friendly", "✅"),
    CAUTION("Keto caution – small portions only", "⚠️"),
    NOT_FRIENDLY("Not keto friendly", "❌"),
}

/** How a specific meal affects today's keto plan, given what was already eaten. */
data class KetoImpact(
    val rating: KetoRating,
    val mealNetCarbs: Double,
    val carbsUsedBefore: Double,
    val dailyLimit: Double,
    val breaksLimit: Boolean,
    val budgetSharePercent: Int,
    val message: String,
)

object KetoRules {
    /** Net carbs per meal at or below this are comfortably keto. */
    const val FRIENDLY_MAX_NET_CARBS = 6.0

    /** Above this, a single meal usually eats most of a strict 20–30 g daily budget. */
    const val CAUTION_MAX_NET_CARBS = 12.0

    /** Share of energy from net carbs that is still keto-style eating. */
    const val MAX_CARB_ENERGY_SHARE = 0.10

    fun rate(n: Nutrition): KetoRating {
        val net = n.netCarbs
        val carbEnergyShare = if (n.calories > 0) (net * 4.0) / n.calories else 0.0
        return when {
            net <= FRIENDLY_MAX_NET_CARBS && carbEnergyShare <= MAX_CARB_ENERGY_SHARE * 2 -> KetoRating.FRIENDLY
            net <= FRIENDLY_MAX_NET_CARBS -> KetoRating.CAUTION
            net <= CAUTION_MAX_NET_CARBS -> KetoRating.CAUTION
            else -> KetoRating.NOT_FRIENDLY
        }
    }

    fun impact(meal: Nutrition, carbsUsedToday: Double, dailyNetCarbLimit: Double): KetoImpact {
        val net = meal.netCarbs
        val rating = rate(meal)
        val limit = max(1.0, dailyNetCarbLimit)
        val after = carbsUsedToday + net
        val breaks = after > limit
        val share = ((net / limit) * 100).roundToInt()
        val message = when {
            breaks && carbsUsedToday >= limit ->
                "You are already over today's ${limit.r0()} g net-carb limit. This adds ${net.r0()} g more and will likely keep you out of ketosis."
            breaks ->
                "This meal has ${net.r0()} g net carbs and takes you ${(after - limit).r0()} g over your ${limit.r0()} g daily limit. It will likely knock you out of ketosis."
            share >= 50 ->
                "Fits, but uses $share% of your daily carb budget. Keep the rest of today very low-carb."
            rating == KetoRating.NOT_FRIENDLY ->
                "High in carbs for one meal, but still within today's budget. Consider a smaller portion."
            else ->
                "Fits your keto plan. ${max(0.0, limit - after).r0()} g net carbs left for today."
        }
        return KetoImpact(rating, net, carbsUsedToday, limit, breaks, share, message)
    }
}
