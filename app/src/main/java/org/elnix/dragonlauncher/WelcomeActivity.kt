package org.elnix.dragonlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ui.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.ui.welcome.WelcomeScreen
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore

class WelcomeActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = WindowInsetsControllerCompat(window, window.decorView)

        lifecycleScope.launch {
            UiSettingsStore.getFullscreen(this@WelcomeActivity).collectLatest { enabled ->
                if (enabled) {
                    controller.hide(
                        WindowInsetsCompat.Type.statusBars() or
                                WindowInsetsCompat.Type.navigationBars()
                    )
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(
                        WindowInsetsCompat.Type.statusBars() or
                                WindowInsetsCompat.Type.navigationBars()
                    )
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val navigateToSettings = remember { mutableStateOf(false) }

            LaunchedEffect(navigateToSettings.value) {
                if (navigateToSettings.value) {
                    startActivity(Intent(this@WelcomeActivity, SettingsActivity::class.java))
                    navigateToSettings.value = false
                }
            }


            // Colors
            val ctx = LocalContext.current

            val primary by ColorSettingsStore.getPrimary(ctx).collectAsState(initial = null)
            val onPrimary by ColorSettingsStore.getOnPrimary(ctx).collectAsState(initial = null)

            val secondary by ColorSettingsStore.getSecondary(ctx).collectAsState(initial = null)
            val onSecondary by ColorSettingsStore.getOnSecondary(ctx).collectAsState(initial = null)

            val tertiary by ColorSettingsStore.getTertiary(ctx).collectAsState(initial = null)
            val onTertiary by ColorSettingsStore.getOnTertiary(ctx).collectAsState(initial = null)

            val background by ColorSettingsStore.getBackground(ctx).collectAsState(initial = null)
            val onBackground by ColorSettingsStore.getOnBackground(ctx).collectAsState(initial = null)

            val surface by ColorSettingsStore.getSurface(ctx).collectAsState(initial = null)
            val onSurface by ColorSettingsStore.getOnSurface(ctx).collectAsState(initial = null)

            val error by ColorSettingsStore.getError(ctx).collectAsState(initial = null)
            val onError by ColorSettingsStore.getOnError(ctx).collectAsState(initial = null)

            val outline by ColorSettingsStore.getOutline(ctx).collectAsState(initial = null)

            val angleLineColor by ColorSettingsStore.getAngleLineColor(ctx).collectAsState(initial = null)
            val circleColor by ColorSettingsStore.getCircleColor(ctx).collectAsState(initial = null)
//            val completeColor by ColorSettingsStore.getComplete(ctx).collectAsState(initial = null)
//
//            val customSelect by ColorSettingsStore.getSelect(ctx).collectAsState(initial = null)
//            val customNoteTypeText by ColorSettingsStore.getNoteTypeText(ctx).collectAsState(initial = null)
//            val customNoteTypeChecklist by ColorSettingsStore.getNoteTypeChecklist(ctx).collectAsState(initial = null)
//            val customNoteTypeDrawing by ColorSettingsStore.getNoteTypeDrawing(ctx).collectAsState(initial = null)


            DragonLauncherTheme(
                customPrimary = primary,
                customOnPrimary = onPrimary,
                customSecondary = secondary,
                customOnSecondary = onSecondary,
                customTertiary = tertiary,
                customOnTertiary = onTertiary,
                customBackground = background,
                customOnBackground = onBackground,
                customSurface = surface,
                customOnSurface = onSurface,
                customError = error,
                customOnError = onError,
                customOutline = outline,
                customAngleLineColor = angleLineColor,
                customCircleColor = circleColor,
//                customComplete = completeColor,
//                customSelect =customSelect,
//                customNoteTypeText = customNoteTypeText,
//                customNoteTypeCheckList = customNoteTypeChecklist,
//                customNoteTypeDrawing = customNoteTypeDrawing
            ) {
                WelcomeScreen(
                    onEnterSettings = {
                        navigateToSettings.value = true
                        finish()
                    },
                    onEnterApp = { finish() }
                )
            }
        }
    }
}