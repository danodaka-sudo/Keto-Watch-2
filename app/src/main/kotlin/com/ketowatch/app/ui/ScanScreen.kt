package com.ketowatch.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ketowatch.app.util.Sharing
import com.ketowatch.core.r0

private val PORTIONS = listOf(0.5 to "½", 1.0 to "1×", 1.5 to "1½", 2.0 to "2×")

@Composable
fun ScanScreen(state: UiState, vm: AppViewModel, photos: PhotoActions) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ vm.back() }) { Icon(Icons.Filled.Close, "Close") }
                Text("Food scan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            when (val scan = state.scan) {
                ScanState.Idle -> Unit
                is ScanState.Analyzing -> AnalyzingView(scan.photoPath)
                is ScanState.Failed -> FailedView(scan, vm, photos)
                is ScanState.Result -> ResultView(state, scan, vm)
            }
        }
    }
}

@Composable
private fun AnalyzingView(photoPath: String) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "alpha",
    )
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (photoPath.isNotBlank()) MealPhoto(photoPath, 220.dp, Modifier.alpha(pulse))
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Looking at your food…", style = MaterialTheme.typography.titleMedium)
        Text(
            "Identifying the dish, portion size and carbs",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FailedView(scan: ScanState.Failed, vm: AppViewModel, photos: PhotoActions) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MealPhoto(scan.photoPath, 180.dp, emoji = "🤔")
        Text(scan.message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (scan.canRetry) Button({ vm.retry() }, Modifier.fillMaxWidth().height(52.dp)) { Text("Try again") }
        FilledTonalButton({ vm.open(Screen.FoodList(scan.photoPath)) }, Modifier.fillMaxWidth().height(52.dp)) {
            Text("🇺🇬 Pick from Ugandan food list")
        }
        OutlinedButton(photos.takePhoto, Modifier.fillMaxWidth().height(52.dp)) { Text("📷 Take another photo") }
    }
}

@Composable
private fun ResultView(state: UiState, scan: ScanState.Result, vm: AppViewModel) {
    val context = LocalContext.current
    val meal = scan.scaled
    val total = meal.total
    val impact = state.impactOf(meal)
    val after = state.summary.total.calories + total.calories
    val leftAfter = state.profile.calorieGoal - after

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box {
                MealPhoto(scan.photoPath, 240.dp, Modifier.fillMaxWidth())
            }
            Column {
                Text(meal.dishName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                val confidence = when (scan.analysis.confidence) {
                    "high" -> "High confidence"
                    "low" -> "Low confidence – check the details below"
                    "manual" -> "From food list"
                    else -> "Medium confidence"
                }
                Text(confidence, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Big keto verdict
            Surface(shape = MaterialTheme.shapes.medium, color = KetoColors.container(impact.rating)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${impact.rating.emoji} ${impact.rating.label}",
                        color = KetoColors.content(impact.rating),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    )
                    Text(impact.message, color = KetoColors.content(impact.rating), style = MaterialTheme.typography.bodyLarge)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Calories", "${total.calories.r0()}", Modifier.weight(1f), "kcal")
                StatTile("Net carbs", "${total.netCarbs.r0()} g", Modifier.weight(1f), "${total.carbs.r0()} carbs − ${total.fiber.r0()} fibre")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Protein", "${total.protein.r0()} g", Modifier.weight(1f))
                StatTile("Fat", "${total.fat.r0()} g", Modifier.weight(1f))
            }
            Text(
                if (leftAfter >= 0) "After this meal: ${leftAfter.r0()} kcal left of your ${state.profile.calorieGoal} kcal goal."
                else "After this meal you'll be ${(-leftAfter).r0()} kcal over your ${state.profile.calorieGoal} kcal goal.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text("How much did you eat?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                PORTIONS.forEachIndexed { i, (value, label) ->
                    SegmentedButton(
                        selected = scan.portion == value,
                        onClick = { vm.setPortion(value) },
                        shape = SegmentedButtonDefaults.itemShape(i, PORTIONS.size),
                    ) { Text(label) }
                }
            }

            Text("What's on the plate", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(Modifier.padding(12.dp)) {
                    meal.items.forEachIndexed { i, item ->
                        if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                val local = item.localName.takeIf { it.isNotBlank() && !it.equals(item.name, true) }
                                Text(item.name + (local?.let { " ($it)" } ?: ""), fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${item.portion.ifBlank { "portion" }} · ~${item.grams.r0()} g · ${item.nutrition.netCarbs.r0()} g net carbs",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${item.nutrition.calories.r0()} kcal", fontWeight = FontWeight.Bold)
                                KetoBadge(com.ketowatch.core.KetoRules.rate(item.nutrition))
                            }
                        }
                    }
                }
            }
            if (scan.analysis.notes.isNotBlank()) {
                Text("💡 ${scan.analysis.notes}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "Estimates from a photo can be off by 20% or more. Adjust the portion if needed.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
            )
            Spacer(Modifier.height(4.dp))
        }

        // Sticky action bar
        Surface(tonalElevation = 3.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ vm.saveScan() }, Modifier.fillMaxWidth().height(54.dp)) {
                    Text("Save to today's meals", style = MaterialTheme.typography.titleMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ vm.open(Screen.FoodList(scan.photoPath)) }, Modifier.weight(1f)) { Text("Not right? Pick food") }
                    OutlinedButton(
                        {
                            val text = "🍽️ ${meal.dishName}\n${total.calories.r0()} kcal · ${total.netCarbs.r0()} g net carbs\n" +
                                "${impact.rating.emoji} ${impact.rating.label}\n— scanned with KETO WATCH"
                            Sharing.shareMeal(context, text, scan.photoPath)
                        },
                        Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Share, null)
                        Text(" Share")
                    }
                }
            }
        }
    }
}
