package com.ketowatch.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ketowatch.app.util.Sharing
import com.ketowatch.core.DailySummary
import com.ketowatch.core.r0
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(state: UiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val p = state.profile
    val days = state.history.entries.sortedByDescending { it.key }
    val summaries = days.map { it.key to DailySummary.of(it.value.sortedBy { m -> m.timestamp }, p.calorieGoal, p.netCarbLimit) }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier.statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Last 30 days", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (summaries.isNotEmpty()) item {
            val ketoDays = summaries.count { it.second.inKetoRange }
            val avg = summaries.map { it.second.total.calories }.average()
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Days logged", summaries.size.toString(), Modifier.weight(1f))
                StatTile("Keto days", "$ketoDays", Modifier.weight(1f), "within carb limit")
                StatTile("Avg kcal", avg.r0().toString(), Modifier.weight(1f), "goal ${p.calorieGoal}")
            }
        } else item {
            Text("No meals logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(summaries, key = { it.first.toString() }) { (day, s) ->
            val key = day.toString()
            Surface(
                Modifier.fillMaxWidth().clickable { expanded = if (expanded == key) null else key },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(dayLabel(day), fontWeight = FontWeight.Bold)
                            Text(
                                "${s.mealCount} meal${if (s.mealCount == 1) "" else "s"} · ${s.total.calories.r0()} kcal · ${s.differenceText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (s.isOverCalories) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(if (s.inKetoRange) "🥑" else "⚠️", style = MaterialTheme.typography.titleLarge)
                        IconButton({ Sharing.shareText(context, summaryText(s, dayLabel(day))) }) {
                            Icon(Icons.Filled.Share, "Share day")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    MacroBar("Net carbs", s.total.netCarbs, s.netCarbLimit.toDouble())
                    AnimatedVisibility(expanded == key) {
                        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.history[day].orEmpty().sortedBy { it.timestamp }.forEach { m -> MealRow(m) {} }
                        }
                    }
                }
            }
        }
    }
}

private fun dayLabel(d: LocalDate): String = when (d) {
    LocalDate.now() -> "Today"
    LocalDate.now().minusDays(1) -> "Yesterday"
    else -> d.format(DateTimeFormatter.ofPattern("EEE d MMM"))
}
