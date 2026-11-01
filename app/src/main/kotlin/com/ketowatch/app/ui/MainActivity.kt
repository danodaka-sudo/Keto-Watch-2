package com.ketowatch.app.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KetoWatchTheme { App(vm) }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refresh() // a new day may have started while the app was in the background
    }
}

/** Camera + gallery launchers shared by every screen. */
class PhotoActions(val takePhoto: () -> Unit, val pickPhoto: () -> Unit)

@Composable
private fun App(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.toast) {
        state.toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.consumeToast()
        }
    }

    if (!state.profile.onboarded) {
        OnboardingScreen(initial = state.profile, onDone = vm::saveProfile)
        return
    }

    // Camera writes into a private cache file shared via FileProvider (no CAMERA permission needed).
    val captureUri: Uri = remember {
        val f = File(context.cacheDir, "camera/capture.jpg").apply { parentFile?.mkdirs() }
        FileProvider.getUriForFile(context, "${context.packageName}.files", f)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.onPhoto(captureUri)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.onPhoto(uri)
    }
    val photos = PhotoActions(
        takePhoto = {
            runCatching { camera.launch(captureUri) }.onFailure {
                Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
            }
        },
        pickPhoto = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
    )

    BackHandler(enabled = state.screen != Screen.Main || state.tab != Tab.TODAY) { vm.back() }

    when (val screen = state.screen) {
        Screen.Scan -> ScanScreen(state, vm, photos)
        is Screen.FoodList -> FoodListScreen(state, vm, screen.photoPath)
        Screen.Main -> Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = state.tab == Tab.TODAY, onClick = { vm.selectTab(Tab.TODAY) },
                        icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Today") },
                    )
                    NavigationBarItem(
                        selected = state.tab == Tab.HISTORY, onClick = { vm.selectTab(Tab.HISTORY) },
                        icon = { Icon(Icons.Filled.DateRange, null) }, label = { Text("History") },
                    )
                    NavigationBarItem(
                        selected = state.tab == Tab.SETTINGS, onClick = { vm.selectTab(Tab.SETTINGS) },
                        icon = { Icon(Icons.Filled.Settings, null) }, label = { Text("Settings") },
                    )
                }
            },
        ) { pad ->
            val m = Modifier.padding(pad)
            when (state.tab) {
                Tab.TODAY -> TodayScreen(state, vm, photos, m)
                Tab.HISTORY -> HistoryScreen(state, m)
                Tab.SETTINGS -> SettingsScreen(state, vm, m)
            }
        }
    }
}
