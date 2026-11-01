package com.ketowatch.app.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ketowatch.app.util.Sharing
import com.ketowatch.core.DailySummary
import com.ketowatch.core.LoggedMeal
import com.ketowatch.core.r0
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun TodayScreen(state: UiState, vm: AppViewModel, photos: PhotoActions, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val s = state.summary
    var selected by remember { mutableStateOf<LoggedMeal?>(null) }

    LazyColumn(
        modifier.statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting(state.profile.name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton({ Sharing.shareText(context, summaryText(s, "Today")) }) {
                    Icon(Icons.Filled.Share, "Share today's summary")
                }
            }
        }
        item { CalorieCard(s) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Meals today", s.mealCount.toString(), Modifier.weight(1f))
                StatTile("Protein", "${s.total.protein.r0()} g", Modifier.weight(1f))
                StatTile("Fat", "${s.total.fat.r0()} g", Modifier.weight(1f))
            }
        }
        item { KetoCard(s) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    photos.takePhoto,
                    Modifier.fillMaxWidth().height(60.dp),
                    shape = MaterialTheme.shapes.medium,
                ) { Text("📷  Scan my food", style = MaterialTheme.typography.titleMedium) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(photos.pickPhoto, Modifier.weight(1f).height(48.dp)) { Text("🖼️ From gallery") }
                    FilledTonalButton({ vm.open(Screen.FoodList()) }, Modifier.weight(1f).height(48.dp)) { Text("🇺🇬 Food list") }
                }
            }
        }
        item {
            Text("Today's meals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
        if (state.today.isEmpty()) {
            item {
                Text(
                    "Nothing logged yet. Snap your breakfast, lunch or supper to start counting.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.today.reversed(), key = { it.id }) { meal -> MealRow(meal) { selected = meal } }
    }

    selected?.let { meal ->
        MealDialog(
            meal,
            onDismiss = { selected = null },
            onDelete = { vm.deleteMeal(meal); selected = null },
            onShare = { Sharing.shareMeal(context, mealShareText(meal), meal.photoPath) },
        )
    }
}

private fun greeting(name: String): String {
    val h = LocalTime.now().hour
    val part = when { h < 12 -> "Good morning"; h < 17 -> "Good afternoon"; else -> "Good evening" }
    return if (name.isBlank()) part else "$part, $name"
}

@Composable
private fun CalorieCard(s: DailySummary) {
    val ringColor = when {
        s.isOverCalories -> MaterialTheme.colorScheme.error
        s.calorieProgress > 0.9f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Ring(s.calorieProgress, 130.dp, 14.dp, ringColor) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.total.calories.r0().toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text("kcal eaten", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Goal ${s.calorieGoal} kcal", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${abs(s.calorieDifference)} kcal",
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    color = if (s.isOverCalories) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
                Text(if (s.isOverCalories) "over your daily goal" else "left for today", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun KetoCard(s: DailySummary) {
    val ok = s.inKetoRange
    val container = if (ok) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val content = if (ok) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Surface(shape = MaterialTheme.shapes.medium, color = container, contentColor = content) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (ok) "🥑 Keto on track" else "⚠️ Over your carb limit today",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            )
            MacroBar("Net carbs", s.total.netCarbs, s.netCarbLimit.toDouble())
            Text(
                if (ok) "${s.netCarbsLeft.r0()} g net carbs left. Fill up on eggs, fish, meat, avocado and greens like dodo and nakati."
                else "Keep the rest of today to zero-carb foods and drink plenty of water to get back on track.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun MealRow(meal: LoggedMeal, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            MealPhoto(meal.photoPath, 56.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(meal.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${timeOf(meal)} · ${meal.nutrition.netCarbs.r0()} g net carbs",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                KetoBadge(meal.keto)
            }
            Text("${meal.nutrition.calories.r0()}\nkcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun timeOf(meal: LoggedMeal): String =
    Instant.ofEpochMilli(meal.timestamp).atZone(ZoneId.systemDefault()).toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

@Composable
private fun MealDialog(meal: LoggedMeal, onDismiss: () -> Unit, onDelete: () -> Unit, onShare: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(meal.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MealPhoto(meal.photoPath, 220.dp, Modifier.fillMaxWidth())
                KetoBadge(meal.keto, short = false)
                val n = meal.nutrition
                Text("${n.calories.r0()} kcal · ${n.netCarbs.r0()} g net carbs (${n.carbs.r0()} g carbs − ${n.fiber.r0()} g fibre)")
                Text("Protein ${n.protein.r0()} g · Fat ${n.fat.r0()} g")
                Text("Logged at ${timeOf(meal)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onShare) { Text("Share") } },
        dismissButton = {
            TextButton(
                { if (confirm) onDelete() else confirm = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(if (confirm) "Tap again to delete" else "Delete") }
        },
    )
}

fun summaryText(s: DailySummary, label: String): String = buildString {
    appendLine("🥑 KETO WATCH – $label")
    appendLine("Meals: ${s.mealCount}")
    appendLine("Calories: ${s.total.calories.r0()} / ${s.calorieGoal} kcal (${s.differenceText})")
    appendLine("Net carbs: ${s.total.netCarbs.r0()} / ${s.netCarbLimit} g ${if (s.inKetoRange) "✅ keto on track" else "⚠️ over limit"}")
    append("Protein ${s.total.protein.r0()} g · Fat ${s.total.fat.r0()} g")
}

fun mealShareText(meal: LoggedMeal): String =
    "🍽️ ${meal.name}\n${meal.nutrition.calories.r0()} kcal · ${meal.nutrition.netCarbs.r0()} g net carbs\n" +
        "${meal.keto.emoji} ${meal.keto.label}\n— tracked with KETO WATCH"
