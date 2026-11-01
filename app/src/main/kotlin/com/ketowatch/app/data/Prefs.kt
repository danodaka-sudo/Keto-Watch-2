package com.ketowatch.app.data

import android.content.Context
import com.ketowatch.app.BuildConfig
import com.ketowatch.core.RecognitionModel

data class Profile(
    val onboarded: Boolean,
    val name: String,
    val calorieGoal: Int,
    val netCarbLimit: Int,
    val apiKey: String,
    val model: RecognitionModel,
) {
    val hasApiKey: Boolean get() = apiKey.isNotBlank()
}

/** Small key-value settings store. SharedPreferences is private to the app. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("keto_watch", Context.MODE_PRIVATE)

    fun load() = Profile(
        onboarded = sp.getBoolean(K_ONBOARDED, false),
        name = sp.getString(K_NAME, "") ?: "",
        calorieGoal = sp.getInt(K_CALORIES, 1800),
        netCarbLimit = sp.getInt(K_CARBS, 25),
        apiKey = (sp.getString(K_API_KEY, null) ?: BuildConfig.DEFAULT_API_KEY).trim(),
        model = runCatching { RecognitionModel.valueOf(sp.getString(K_MODEL, null) ?: "") }
            .getOrDefault(RecognitionModel.BEST),
    )

    fun save(p: Profile) {
        sp.edit()
            .putBoolean(K_ONBOARDED, p.onboarded)
            .putString(K_NAME, p.name.trim())
            .putInt(K_CALORIES, p.calorieGoal)
            .putInt(K_CARBS, p.netCarbLimit)
            .putString(K_API_KEY, p.apiKey.trim())
            .putString(K_MODEL, p.model.name)
            .apply()
    }

    private companion object {
        const val K_ONBOARDED = "onboarded"
        const val K_NAME = "name"
        const val K_CALORIES = "calorie_goal"
        const val K_CARBS = "net_carb_limit"
        const val K_API_KEY = "api_key"
        const val K_MODEL = "model"
    }
}
