package com.ketowatch.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KetoRulesTest {
    @Test fun matookeIsNotKeto() =
        assertEquals(KetoRating.NOT_FRIENDLY, UgandaFoodDatabase.byId("matooke")!!.keto)

    @Test fun luwomboIsCautionOrBetter() =
        assertTrue(UgandaFoodDatabase.byId("luwombo_chicken")!!.keto != KetoRating.NOT_FRIENDLY)

    @Test fun eggsAvocadoMuchomoAreKeto() {
        listOf("eggs_boiled", "avocado", "muchomo", "fried_tilapia", "cheese").forEach {
            assertEquals(KetoRating.FRIENDLY, UgandaFoodDatabase.byId(it)!!.keto, it)
        }
    }

    @Test fun waterIsKeto() = assertEquals(KetoRating.FRIENDLY, UgandaFoodDatabase.byId("water")!!.keto)

    @Test fun impactDetectsBreakingDailyLimit() {
        val rolex = UgandaFoodDatabase.byId("rolex")!!.nutrition
        val impact = KetoRules.impact(rolex, carbsUsedToday = 5.0, dailyNetCarbLimit = 25.0)
        assertTrue(impact.breaksLimit)
        assertTrue(impact.message.contains("over"))
    }

    @Test fun impactFitsWhenLow() {
        val eggs = UgandaFoodDatabase.byId("eggs_boiled")!!.nutrition
        val impact = KetoRules.impact(eggs, 0.0, 25.0)
        assertFalse(impact.breaksLimit)
        assertTrue(impact.message.startsWith("Fits"))
    }
}

class SummaryTest {
    private fun meal(kcal: Double, carbs: Double) =
        LoggedMeal(0, 0, "x", Nutrition(kcal, carbs, 0.0, 0.0, 0.0), null, "test")

    @Test fun countsMealsAndDifference() {
        val s = DailySummary.of(listOf(meal(500.0, 10.0), meal(700.0, 5.0), meal(300.0, 2.0)), 1800, 25)
        assertEquals(3, s.mealCount)
        assertEquals(1500, s.total.calories.r0())
        assertEquals(300, s.calorieDifference)
        assertEquals("300 kcal left of your goal", s.differenceText)
        assertTrue(s.inKetoRange)
    }

    @Test fun overGoal() {
        val s = DailySummary.of(listOf(meal(2000.0, 40.0)), 1800, 25)
        assertEquals(-200, s.calorieDifference)
        assertEquals("200 kcal over your goal", s.differenceText)
        assertFalse(s.inKetoRange)
    }

    @Test fun calorieCalculator() {
        // 30-year-old woman, 70 kg, 165 cm: BMR = 700 + 1031.25 - 150 - 161 = 1420.25
        assertEquals(1420.25, CalorieCalculator.bmr(Sex.FEMALE, 70.0, 165.0, 30), 0.01)
        val t = CalorieCalculator.dailyTarget(Sex.FEMALE, 70.0, 165.0, 30, Activity.LIGHT, GoalType.LOSE)
        assertEquals(1450, t) // 1420.25*1.375 - 500 = 1452.8 -> 1450
    }
}

class DatabaseTest {
    @Test fun idsUnique() {
        val ids = UgandaFoodDatabase.foods.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun searchByLocalName() {
        assertTrue(UgandaFoodDatabase.search("binyebwa").any { it.id == "groundnut_sauce" })
        assertTrue(UgandaFoodDatabase.search("ROLEX").any { it.id == "rolex" })
    }

    @Test fun caloriesRoughlyMatchMacros() {
        // Atwater check: 4/4/9 with fibre ~2 kcal/g; allow generous tolerance for alcohol & rounding.
        UgandaFoodDatabase.foods.filter { it.category != FoodCategory.DRINK || it.id != "beer" && it.id != "waragi" }.forEach { f ->
            val n = f.nutrition
            val est = (n.carbs - n.fiber) * 4 + n.fiber * 2 + n.protein * 4 + n.fat * 9
            val tol = maxOf(40.0, n.calories * 0.25)
            assertTrue(kotlin.math.abs(est - n.calories) <= tol, "${f.id}: listed ${n.calories} vs macros $est")
        }
    }
}

class ParseTest {
    @Test fun parsesModelJson() {
        val json = """
            {"is_food":true,"dish_name":"Matooke with groundnut sauce","confidence":"high","notes":"Standard plate.",
             "items":[
               {"name":"Matooke","local_name":"Matooke","portion":"1 plate","grams":250,"calories":290,"carbs_g":77,"fiber_g":5.8,"protein_g":3,"fat_g":0.5},
               {"name":"Groundnut sauce","local_name":"Binyebwa","portion":"1 cup","grams":200,"calories":300,"carbs_g":14,"fiber_g":5,"protein_g":13,"fat_g":23}
             ]}
        """.trimIndent()
        val m = FoodRecognizer.parse(json)
        assertEquals(2, m.items.size)
        assertEquals(590, m.total.calories.r0())
        assertEquals(KetoRating.NOT_FRIENDLY, m.keto)
        assertEquals(295, m.scaled(0.5).total.calories.r0())
    }

    @Test fun parsesNotFood() {
        val m = FoodRecognizer.parse("""{"is_food":false,"dish_name":"","items":[],"confidence":"low","notes":"That's a shoe."}""")
        assertFalse(m.isFood)
        assertEquals("Meal", m.dishName)
    }

    @Test fun requestBuilds() {
        val p = FoodRecognizer("sk-test").buildRequest(byteArrayOf(1, 2, 3), "lunch from a restaurant in Kampala")
        assertTrue(p.model().asString() == "claude-opus-5-5")
        assertTrue(FoodRecognizer.SYSTEM_PROMPT.contains("Luwombo"))
    }
}
