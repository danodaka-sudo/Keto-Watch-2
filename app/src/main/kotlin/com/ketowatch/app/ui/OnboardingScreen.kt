package com.ketowatch.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ketowatch.app.data.Profile
import com.ketowatch.core.Activity
import com.ketowatch.core.CalorieCalculator
import com.ketowatch.core.GoalType
import com.ketowatch.core.Sex

private const val STEPS = 4

@Composable
fun OnboardingScreen(initial: Profile, onDone: (Profile) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var calories by rememberSaveable { mutableIntStateOf(initial.calorieGoal) }
    var carbs by rememberSaveable { mutableIntStateOf(initial.netCarbLimit) }
    var apiKey by rememberSaveable { mutableStateOf(initial.apiKey) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(24.dp),
        ) {
            if (step > 0) {
                LinearProgressIndicator(
                    progress = { step / STEPS.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    drawStopIndicator = {},
                )
                Spacer(Modifier.height(24.dp))
            }
            AnimatedContent(step, label = "step", modifier = Modifier.weight(1f)) { s ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    when (s) {
                        0 -> Welcome()
                        1 -> {
                            Title("What should we call you?")
                            OutlinedTextField(
                                name, { name = it }, Modifier.fillMaxWidth(),
                                label = { Text("Your first name") }, singleLine = true,
                            )
                        }
                        2 -> {
                            Title("Your daily calorie goal")
                            Body("This is the number of calories you want to eat each day. KETO WATCH compares every day's total against it.")
                            Spacer(Modifier.height(16.dp))
                            CalorieGoalEditor(calories) { calories = it }
                        }
                        3 -> {
                            Title("Your keto carb limit")
                            Body("Most keto plans keep net carbs (carbs minus fibre) between 20 and 50 g a day. 25 g is a good strict starting point.")
                            Spacer(Modifier.height(24.dp))
                            Text("$carbs g net carbs / day", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Slider(carbs.toFloat(), { carbs = it.toInt() }, valueRange = 15f..60f, steps = 44)
                            Body(
                                when {
                                    carbs <= 25 -> "Strict keto – best for getting into ketosis."
                                    carbs <= 40 -> "Moderate keto."
                                    else -> "Relaxed low-carb."
                                },
                            )
                        }
                        else -> {
                            Title("Turn on photo recognition")
                            Body(
                                "KETO WATCH uses Claude AI to recognise food in your photos – including Ugandan dishes like matooke, luwombo, rolex and malakwang. " +
                                    "Paste your Claude API key from console.anthropic.com. You can skip this and add it later in Settings; " +
                                    "you can still log meals from the built-in Ugandan food list.",
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                apiKey, { apiKey = it }, Modifier.fillMaxWidth(),
                                label = { Text("Claude API key (optional)") }, singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (step > 0) OutlinedButton({ step-- }, Modifier.weight(1f).height(52.dp)) { Text("Back") }
                Button(
                    onClick = {
                        if (step < STEPS) step++ else onDone(
                            initial.copy(onboarded = true, name = name, calorieGoal = calories, netCarbLimit = carbs, apiKey = apiKey),
                        )
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = step != 1 || name.isNotBlank(),
                ) { Text(if (step == 0) "Get started" else if (step < STEPS) "Next" else "Finish") }
            }
        }
    }
}

@Composable
private fun Welcome() {
    Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🥑", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("KETO WATCH", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(
            "Snap your food. Know your calories.\nStay in ketosis.",
            style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        listOf(
            "📷" to "Recognises Ugandan meals – matooke, posho, luwombo, rolex, nsenene – and supermarket items",
            "🔥" to "Counts your meals and calories every day",
            "🥑" to "Tells you if a meal fits your keto diet",
        ).forEach { (e, t) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(e, fontSize = 24.sp)
                Spacer(Modifier.padding(6.dp))
                Text(t, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun Title(t: String) {
    Text(t, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Body(t: String) =
    Text(t, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

/** Manual entry plus a "work it out for me" calculator. Shared by onboarding and settings. */
@Composable
fun CalorieGoalEditor(value: Int, onChange: (Int) -> Unit) {
    var showCalc by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        text,
        { t ->
            text = t.filter(Char::isDigit).take(4)
            text.toIntOrNull()?.takeIf { it in 800..6000 }?.let(onChange)
        },
        Modifier.fillMaxWidth(),
        label = { Text("Calories per day (kcal)") },
        singleLine = true,
        supportingText = { Text("Between 800 and 6000") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    TextButton({ showCalc = !showCalc }) { Text(if (showCalc) "Hide calculator" else "Not sure? Calculate it for me") }
    if (showCalc) GoalCalculator { onChange(it); text = it.toString(); showCalc = false }
}

@Composable
private fun GoalCalculator(onResult: (Int) -> Unit) {
    var sex by rememberSaveable { mutableStateOf(Sex.FEMALE) }
    var age by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var activity by rememberSaveable { mutableStateOf(Activity.LIGHT) }
    var goal by rememberSaveable { mutableStateOf(GoalType.LOSE) }

    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sex.entries.forEach { s ->
                    FilterChip(sex == s, { sex = s }, { Text(s.name.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumField(age, { age = it }, "Age", Modifier.weight(1f))
                NumField(weight, { weight = it }, "Weight kg", Modifier.weight(1f))
                NumField(height, { height = it }, "Height cm", Modifier.weight(1f))
            }
            Text("Activity", style = MaterialTheme.typography.labelLarge)
            Activity.entries.forEach { a ->
                FilterChip(activity == a, { activity = a }, { Text(a.label) }, Modifier.fillMaxWidth())
            }
            Text("Goal", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalType.entries.forEach { g -> FilterChip(goal == g, { goal = g }, { Text(g.label) }) }
            }
            val a = age.toIntOrNull()
            val w = weight.toDoubleOrNull()
            val h = height.toDoubleOrNull()
            val valid = a != null && a in 12..100 && w != null && w in 25.0..300.0 && h != null && h in 100.0..250.0
            Button(
                onClick = { onResult(CalorieCalculator.dailyTarget(sex, w!!, h!!, a!!, activity, goal)) },
                enabled = valid, modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (valid) "Use ${CalorieCalculator.dailyTarget(sex, w!!, h!!, a!!, activity, goal)} kcal / day" else "Fill in age, weight and height")
            }
        }
    }
}

@Composable
private fun NumField(v: String, onV: (String) -> Unit, label: String, modifier: Modifier) =
    OutlinedTextField(
        v, { onV(it.filter { c -> c.isDigit() || c == '.' }.take(5)) }, modifier,
        label = { Text(label, maxLines = 1) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
