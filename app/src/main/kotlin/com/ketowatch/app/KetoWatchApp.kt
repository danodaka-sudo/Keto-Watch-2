package com.ketowatch.app

import android.app.Application
import com.ketowatch.app.data.MealRepository
import com.ketowatch.app.data.Prefs

class KetoWatchApp : Application() {
    lateinit var prefs: Prefs
        private set
    lateinit var meals: MealRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        meals = MealRepository(this)
    }
}
