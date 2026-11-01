package com.ketowatch.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ketowatch.app.BuildConfig
import com.ketowatch.app.util.Sharing
import com.ketowatch.core.RecognitionModel

@Composable
fun SettingsScreen(state: UiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val p = state.profile
    var name by remember(p) { mutableStateOf(p.name) }
    var calories by remember(p) { mutableIntStateOf(p.calorieGoal) }
    var carbs by remember(p) { mutableIntStateOf(p.netCarbLimit) }
    var apiKey by remember(p) { mutableStateOf(p.apiKey) }
    var model by remember(p) { mutableStateOf(p.model) }
    var confirmClear by remember { mutableStateOf(false) }
    val dirty = name != p.name || calories != p.calorieGoal || carbs != p.netCarbLimit || apiKey != p.apiKey || model != p.model

    Column(
        modifier.statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Section("Share KETO WATCH") {
            Text("Send the app to friends and family – they can install it straight from WhatsApp, Bluetooth, Nearby Share or Xender.")
            Button({ Sharing.shareApp(context) }, Modifier.fillMaxWidth().height(52.dp)) { Text("📲  Share the app") }
            FilledTonalButton(
                {
                    Sharing.shareText(
                        context,
                        "I'm tracking my meals with KETO WATCH 🥑 – it recognises Ugandan food from a photo and tells you the calories and if it's keto friendly. Ask me for the app!",
                    )
                },
                Modifier.fillMaxWidth(),
            ) { Text("Send an invite message") }
        }

        Section("Profile & goals") {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
            CalorieGoalEditor(calories) { calories = it }
            Text("Net carb limit: $carbs g / day", fontWeight = FontWeight.SemiBold)
            Slider(carbs.toFloat(), { carbs = it.toInt() }, valueRange = 15f..60f, steps = 44)
        }

        Section("Photo recognition (Claude AI)") {
            OutlinedTextField(
                apiKey, { apiKey = it }, Modifier.fillMaxWidth(),
                label = { Text("Claude API key") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = { Text("Get one at console.anthropic.com. Stored only on this phone.") },
            )
            RecognitionModel.entries.forEach { m ->
                Row(
                    Modifier.fillMaxWidth().clickable { model = m },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(model == m, { model = m })
                    Text(m.label)
                }
            }
        }

        Button(
            { vm.saveProfile(p.copy(name = name, calorieGoal = calories, netCarbLimit = carbs, apiKey = apiKey, model = model)) },
            Modifier.fillMaxWidth().height(52.dp),
            enabled = dirty,
        ) { Text(if (dirty) "Save changes" else "Saved") }

        Section("Data") {
            OutlinedButton({ confirmClear = true }, Modifier.fillMaxWidth()) { Text("Clear all meal history") }
        }

        Text(
            "KETO WATCH ${BuildConfig.VERSION_NAME}\nNutrition values are estimates for guidance only and are not medical advice. " +
                "If you have diabetes, kidney disease, are pregnant or take medication, talk to a health worker before starting keto.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all meals?") },
            text = { Text("This deletes every logged meal and photo. Your goals stay.") },
            confirmButton = { TextButton({ vm.clearAllData(); confirmClear = false }) { Text("Delete everything") } },
            dismissButton = { TextButton({ confirmClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}
