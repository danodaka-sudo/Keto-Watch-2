package com.ketowatch.core

import kotlin.math.max
import kotlin.math.roundToInt

enum class Sex { MALE, FEMALE }

enum class Activity(val label: String, val factor: Double) {
    SEDENTARY("Mostly sitting", 1.2),
    LIGHT("Light (walks, 1–3 workouts/week)", 1.375),
    MODERATE("Moderate (3–5 workouts/week)", 1.55),
    ACTIVE("Very active / physical job", 1.725),
}

enum class GoalType(val label: String, val kcalAdjustment: Int) {
    LOSE("Lose weight", -500),
    MAINTAIN("Maintain weight", 0),
    GAIN("Gain weight", 300),
}

object CalorieCalculator {
    /** Mifflin–St Jeor resting energy expenditure. */
    fun bmr(sex: Sex, weightKg: Double, heightCm: Double, age: Int): Double {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return if (sex == Sex.MALE) base + 5 else base - 161
    }

    /** Suggested daily calories, rounded to the nearest 50 kcal and never below a safe floor. */
    fun dailyTarget(sex: Sex, weightKg: Double, heightCm: Double, age: Int, activity: Activity, goal: GoalType): Int {
        val tdee = bmr(sex, weightKg, heightCm, age) * activity.factor
        val floor = if (sex == Sex.MALE) 1500 else 1200
        val target = max(floor.toDouble(), tdee + goal.kcalAdjustment)
        return ((target / 50).roundToInt() * 50)
    }
}

/** A meal as stored in the diary. */
data class LoggedMeal(
    val id: Long,
    val timestamp: Long,
    val name: String,
    val nutrition: Nutrition,
    val photoPath: String?,
    val source: String,
) {
    val keto: KetoRating get() = KetoRules.rate(nutrition)
}

data class DailySummary(
    val mealCount: Int,
    val total: Nutrition,
    val calorieGoal: Int,
    val netCarbLimit: Int,
) {
    /** Positive = calories still available; negative = over the goal. */
    val calorieDifference: Int get() = calorieGoal - total.calories.roundToInt()
    val isOverCalories: Boolean get() = calorieDifference < 0
    val netCarbsLeft: Double get() = netCarbLimit - total.netCarbs
    val calorieProgress: Float get() = if (calorieGoal <= 0) 0f else (total.calories / calorieGoal).toFloat()
    val carbProgress: Float get() = if (netCarbLimit <= 0) 0f else (total.netCarbs / netCarbLimit).toFloat()
    val inKetoRange: Boolean get() = total.netCarbs <= netCarbLimit

    val differenceText: String
        get() = if (isOverCalories) "${-calorieDifference} kcal over your goal"
        else "$calorieDifference kcal left of your goal"

    companion object {
        fun of(meals: List<LoggedMeal>, calorieGoal: Int, netCarbLimit: Int) = DailySummary(
            mealCount = meals.size,
            total = meals.fold(Nutrition.ZERO) { acc, m -> acc + m.nutrition },
            calorieGoal = calorieGoal,
            netCarbLimit = netCarbLimit,
        )
    }
}
