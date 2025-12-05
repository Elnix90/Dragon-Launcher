package org.elnix.dragonlauncher

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.MainAppUi
import org.elnix.dragonlauncher.ui.ROUTES
import org.elnix.dragonlauncher.ui.settings.backup.BackupViewModel
import org.elnix.dragonlauncher.ui.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val appsViewModel : AppDrawerViewModel by viewModels()
    private val backupViewModel : BackupViewModel by viewModels()

    private var navControllerHolder = mutableStateOf<NavHostController?>(null)


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {

        // Use hardware acceleration
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)

        lifecycleScope.launch {
            UiSettingsStore.getFullscreen(this@MainActivity).collectLatest { enabled ->
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

//        enableEdgeToEdge()
        setContent {
            val ctx = LocalContext.current

            val hasInitialized by PrivateSettingsStore.getHasInitialized(ctx)
                .collectAsState(initial = true)

            LaunchedEffect(hasInitialized) {
                if (!hasInitialized) {
                    SwipeSettingsStore.save(ctx,
                        listOf(
                            SwipePointSerializable(
                                circleNumber = 0,
                                angleDeg = 0.toDouble(),
                                action = SwipeActionSerializable.OpenAppDrawer,
                                id = UUID.randomUUID().toString()
                            ),
                            SwipePointSerializable(
                                circleNumber = 1,
                                angleDeg = 200.toDouble(),
                                action = SwipeActionSerializable.NotificationShade,
                                id = UUID.randomUUID().toString()
                            ),
                            SwipePointSerializable(
                                circleNumber = 1,
                                angleDeg = 160.toDouble(),
                                action = SwipeActionSerializable.ControlPanel,
                                id = UUID.randomUUID().toString()
                            )
                        )
                    )
                    PrivateSettingsStore.setHasInitialized(ctx, true)
                }
            }

            // Colors

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

                val navController = rememberNavController()
                navControllerHolder.value = navController

                MainAppUi(
                    backupViewModel = backupViewModel,
                    appsViewModel = appsViewModel,
                    navController = navController
                )
            }
        }
    }

//    override fun onPause() {
//        super.onPause()
//
//        // Clear entire navigation stack
//        navControllerHolder.value?.let { nav ->
//            nav.navigate(ROUTES.MAIN) {
//                popUpTo(0) { inclusive = true }
//            }
//        }
//    }
}
