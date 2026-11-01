package com.ketowatch.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ketowatch.core.FoodCategory
import com.ketowatch.core.FoodItem
import com.ketowatch.core.MealAnalysis
import com.ketowatch.core.UgandaFoodDatabase
import com.ketowatch.core.r0

private val SERVINGS = listOf(0.5 to "½", 1.0 to "1", 1.5 to "1½", 2.0 to "2", 3.0 to "3")

@Composable
fun FoodListScreen(state: UiState, vm: AppViewModel, photoPath: String?) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<FoodCategory?>(null) }
    var picked by remember { mutableStateOf<FoodItem?>(null) }
    val results = remember(query, category) {
        UgandaFoodDatabase.search(query).filter { category == null || it.category == category }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ vm.back() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                Text("Ugandan food list", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
                query, { query = it },
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search: matooke, rolex, binyebwa, Nile Special…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { FilterChip(category == null, { category = null }, { Text("All") }) }
                items(FoodCategory.entries) { c ->
                    FilterChip(category == c, { category = if (category == c) null else c }, { Text(c.label) })
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (results.isEmpty()) item {
                    Text(
                        "No match. Try another name, or scan a photo so the AI can recognise it.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(results, key = { it.id }) { food -> FoodRow(food) { picked = food } }
            }
        }
    }

    picked?.let { food ->
        AddFoodDialog(
            food, state,
            onDismiss = { picked = null },
            onAdd = { servings -> picked = null; vm.pickFood(food, servings, photoPath) },
        )
    }
}

@Composable
private fun FoodRow(food: FoodItem, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(food.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    food.localNames.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${food.serving} · ${food.nutrition.netCarbs.r0()} g net carbs",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${food.nutrition.calories.r0()} kcal", fontWeight = FontWeight.Bold)
                KetoBadge(food.keto)
            }
        }
    }
}

@Composable
private fun AddFoodDialog(food: FoodItem, state: UiState, onDismiss: () -> Unit, onAdd: (Double) -> Unit) {
    var servings by remember { mutableDoubleStateOf(1.0) }
    val meal = MealAnalysis.fromFood(food, servings)
    val impact = state.impactOf(meal)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1 serving = ${food.serving} (~${food.servingGrams} g)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Servings", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SERVINGS.forEachIndexed { i, (v, label) ->
                        SegmentedButton(
                            selected = servings == v, onClick = { servings = v },
                            shape = SegmentedButtonDefaults.itemShape(i, SERVINGS.size),
                        ) { Text(label) }
                    }
                }
                val t = meal.total
                Text(
                    "${t.calories.r0()} kcal · ${t.netCarbs.r0()} g net carbs · ${t.protein.r0()} g protein · ${t.fat.r0()} g fat",
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(shape = MaterialTheme.shapes.small, color = KetoColors.container(impact.rating)) {
                    Column(Modifier.padding(10.dp)) {
                        Text("${impact.rating.emoji} ${impact.rating.label}", color = KetoColors.content(impact.rating), fontWeight = FontWeight.Bold)
                        Text(impact.message, color = KetoColors.content(impact.rating), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = { TextButton({ onAdd(servings) }) { Text("Add meal") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}
