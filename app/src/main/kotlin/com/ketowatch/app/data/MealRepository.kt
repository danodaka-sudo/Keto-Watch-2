package com.ketowatch.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ketowatch.core.LoggedMeal
import com.ketowatch.core.Nutrition
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Meal diary stored in a tiny SQLite database (no extra libraries needed).
 * Every meal has a timestamp, so "meals today" and day totals are simple range queries.
 */
class MealRepository(context: Context) :
    SQLiteOpenHelper(context, "keto_watch.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE meals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ts INTEGER NOT NULL,
                name TEXT NOT NULL,
                kcal REAL NOT NULL,
                carbs REAL NOT NULL,
                fiber REAL NOT NULL,
                protein REAL NOT NULL,
                fat REAL NOT NULL,
                photo TEXT,
                source TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX idx_meals_ts ON meals(ts)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun insert(name: String, n: Nutrition, photoPath: String?, source: String, timestamp: Long = System.currentTimeMillis()): Long =
        writableDatabase.insert(
            "meals", null,
            ContentValues().apply {
                put("ts", timestamp)
                put("name", name)
                put("kcal", n.calories)
                put("carbs", n.carbs)
                put("fiber", n.fiber)
                put("protein", n.protein)
                put("fat", n.fat)
                put("photo", photoPath)
                put("source", source)
            },
        )

    fun delete(meal: LoggedMeal) {
        writableDatabase.delete("meals", "id = ?", arrayOf(meal.id.toString()))
        meal.photoPath?.let { runCatching { File(it).delete() } }
    }

    fun forDay(day: LocalDate): List<LoggedMeal> {
        val zone = ZoneId.systemDefault()
        val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return query("ts >= ? AND ts < ?", arrayOf(start.toString(), end.toString()), "ts ASC")
    }

    /** Meals from the last [days] days, newest first, grouped by calendar day. */
    fun history(days: Int): Map<LocalDate, List<LoggedMeal>> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().minusDays(days.toLong() - 1).atStartOfDay(zone).toInstant().toEpochMilli()
        return query("ts >= ?", arrayOf(start.toString()), "ts DESC")
            .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
    }

    fun clearAll() {
        writableDatabase.delete("meals", null, null)
    }

    private fun query(where: String, args: Array<String>, order: String): List<LoggedMeal> =
        readableDatabase.query("meals", null, where, args, null, null, order).use { c ->
            buildList { while (c.moveToNext()) add(c.toMeal()) }
        }

    private fun Cursor.toMeal() = LoggedMeal(
        id = getLong(getColumnIndexOrThrow("id")),
        timestamp = getLong(getColumnIndexOrThrow("ts")),
        name = getString(getColumnIndexOrThrow("name")),
        nutrition = Nutrition(
            getDouble(getColumnIndexOrThrow("kcal")),
            getDouble(getColumnIndexOrThrow("carbs")),
            getDouble(getColumnIndexOrThrow("fiber")),
            getDouble(getColumnIndexOrThrow("protein")),
            getDouble(getColumnIndexOrThrow("fat")),
        ),
        photoPath = getString(getColumnIndexOrThrow("photo")),
        source = getString(getColumnIndexOrThrow("source")),
    )
}
