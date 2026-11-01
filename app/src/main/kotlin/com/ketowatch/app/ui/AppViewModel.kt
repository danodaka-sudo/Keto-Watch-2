package com.ketowatch.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ketowatch.app.KetoWatchApp
import com.ketowatch.app.data.Profile
import com.ketowatch.app.util.Images
import com.ketowatch.core.DailySummary
import com.ketowatch.core.FoodItem
import com.ketowatch.core.FoodRecognizer
import com.ketowatch.core.KetoImpact
import com.ketowatch.core.KetoRules
import com.ketowatch.core.LoggedMeal
import com.ketowatch.core.MealAnalysis
import com.ketowatch.core.RecognitionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

enum class Tab { TODAY, HISTORY, SETTINGS }

sealed interface Screen {
    data object Main : Screen
    data object Scan : Screen
    /** [photoPath] set when correcting a scanned photo by picking from the list. */
    data class FoodList(val photoPath: String? = null) : Screen
}

sealed interface ScanState {
    data object Idle : ScanState
    data class Analyzing(val photoPath: String) : ScanState
    data class Result(
        val analysis: MealAnalysis,
        val photoPath: String?,
        val portion: Double = 1.0,
    ) : ScanState {
        val scaled: MealAnalysis get() = analysis.scaled(portion)
    }
    data class Failed(val message: String, val photoPath: String?, val canRetry: Boolean) : ScanState
}

data class UiState(
    val profile: Profile,
    val tab: Tab = Tab.TODAY,
    val screen: Screen = Screen.Main,
    val today: List<LoggedMeal> = emptyList(),
    val history: Map<LocalDate, List<LoggedMeal>> = emptyMap(),
    val scan: ScanState = ScanState.Idle,
    val toast: String? = null,
) {
    val summary: DailySummary get() = DailySummary.of(today, profile.calorieGoal, profile.netCarbLimit)

    fun impactOf(meal: MealAnalysis): KetoImpact =
        KetoRules.impact(meal.total, summary.total.netCarbs, profile.netCarbLimit.toDouble())
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val kw = app as KetoWatchApp
    private val _state = MutableStateFlow(UiState(profile = kw.prefs.load()))
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Keeps the last photo bytes so "Try again" doesn't need a new photo. */
    private var lastPhotoBytes: ByteArray? = null

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val (today, history) = withContext(Dispatchers.IO) {
            kw.meals.forDay(LocalDate.now()) to kw.meals.history(30)
        }
        _state.update { it.copy(today = today, history = history) }
    }

    // ---------- navigation ----------
    fun selectTab(tab: Tab) = _state.update { it.copy(tab = tab, screen = Screen.Main) }
    fun open(screen: Screen) = _state.update { it.copy(screen = screen) }

    fun back(): Boolean {
        val s = _state.value
        if (s.screen == Screen.Main) {
            if (s.tab != Tab.TODAY) { selectTab(Tab.TODAY); return true }
            return false
        }
        // Leaving the scan flow without saving: remove the orphan photo.
        val screen = s.screen
        // Came to the food list from a scan: go back to the scan result instead of closing it.
        if (screen is Screen.FoodList && screen.photoPath != null && s.scan !is ScanState.Idle) {
            _state.update { it.copy(screen = Screen.Scan) }
            return true
        }
        if (screen == Screen.Scan) discardScanPhoto()
        _state.update { it.copy(screen = Screen.Main, scan = ScanState.Idle) }
        return true
    }

    fun consumeToast() = _state.update { it.copy(toast = null) }

    // ---------- profile ----------
    fun saveProfile(p: Profile) {
        kw.prefs.save(p)
        _state.update { it.copy(profile = p) }
    }

    // ---------- scanning ----------
    fun onPhoto(uri: Uri) {
        discardScanPhoto() // "Take another photo" replaces the previous unsaved one
        _state.update { it.copy(screen = Screen.Scan, scan = ScanState.Analyzing("")) }
        viewModelScope.launch {
            val prepared = withContext(Dispatchers.IO) {
                runCatching { Images.prepare(getApplication(), uri) }
            }
            prepared.onSuccess { (file, bytes) ->
                lastPhotoBytes = bytes
                analyze(file.absolutePath, bytes)
            }.onFailure {
                _state.update { s -> s.copy(scan = ScanState.Failed("Couldn't read that photo. Please try again.", null, false)) }
            }
        }
    }

    fun retry() {
        val s = _state.value.scan
        val bytes = lastPhotoBytes
        val path = (s as? ScanState.Failed)?.photoPath
        if (bytes != null && path != null) analyze(path, bytes)
    }

    private fun analyze(photoPath: String, bytes: ByteArray) {
        val profile = _state.value.profile
        if (!profile.hasApiKey) {
            _state.update {
                it.copy(
                    scan = ScanState.Failed(
                        "Photo recognition needs a Claude API key (add one in Settings). " +
                            "You can still log this meal by picking it from the Ugandan food list.",
                        photoPath, canRetry = false,
                    ),
                )
            }
            return
        }
        _state.update { it.copy(scan = ScanState.Analyzing(photoPath)) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { FoodRecognizer(profile.apiKey, profile.model).analyze(bytes) }
            }
            _state.update { s ->
                // The user may have left the scan screen while we were waiting.
                if (s.screen != Screen.Scan) return@update s
                result.fold(
                    onSuccess = { a ->
                        if (!a.isFood || a.items.isEmpty()) {
                            s.copy(scan = ScanState.Failed(a.notes.ifBlank { "No food found in this photo." }, photoPath, canRetry = false))
                        } else {
                            s.copy(scan = ScanState.Result(a, photoPath))
                        }
                    },
                    onFailure = { e ->
                        val msg = (e as? RecognitionException)?.message ?: "Something went wrong. Please try again."
                        s.copy(scan = ScanState.Failed(msg, photoPath, canRetry = true))
                    },
                )
            }
        }
    }

    fun setPortion(portion: Double) = _state.update { s ->
        val r = s.scan as? ScanState.Result ?: return@update s
        s.copy(scan = r.copy(portion = portion))
    }

    fun saveScan() {
        val r = _state.value.scan as? ScanState.Result ?: return
        val meal = r.scaled
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                kw.meals.insert(meal.dishName, meal.total, r.photoPath, if (r.analysis.confidence == "manual") "list" else "photo")
            }
            lastPhotoBytes = null
            _state.update { it.copy(screen = Screen.Main, tab = Tab.TODAY, scan = ScanState.Idle, toast = "Meal saved") }
            refresh()
        }
    }

    /** From the food list: either log directly, or (when correcting a scan) show it as the scan result. */
    fun pickFood(food: FoodItem, servings: Double, photoPath: String?) {
        val analysis = MealAnalysis.fromFood(food, servings)
        viewModelScope.launch {
            withContext(Dispatchers.IO) { kw.meals.insert(analysis.dishName, analysis.total, photoPath, "list") }
            lastPhotoBytes = null
            _state.update { it.copy(screen = Screen.Main, tab = Tab.TODAY, scan = ScanState.Idle, toast = "${food.name} added") }
            refresh()
        }
    }

    private fun discardScanPhoto() {
        val path = when (val s = _state.value.scan) {
            is ScanState.Result -> s.photoPath
            is ScanState.Failed -> s.photoPath
            is ScanState.Analyzing -> s.photoPath
            ScanState.Idle -> null
        }
        path?.takeIf { it.isNotBlank() }?.let { runCatching { File(it).delete() } }
        lastPhotoBytes = null
    }

    fun deleteMeal(meal: LoggedMeal) = viewModelScope.launch {
        withContext(Dispatchers.IO) { kw.meals.delete(meal) }
        _state.update { it.copy(toast = "Meal deleted") }
        refresh()
    }

    fun clearAllData() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            kw.meals.clearAll()
            File(getApplication<Application>().filesDir, "meals").deleteRecursively()
        }
        _state.update { it.copy(toast = "All meals cleared") }
        refresh()
    }
}
