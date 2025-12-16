package org.elnix.dragonlauncher

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
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
import org.elnix.dragonlauncher.ui.SETTINGS
import org.elnix.dragonlauncher.ui.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.utils.models.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.utils.models.BackupViewModel
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val appLifecycleViewModel : AppLifecycleViewModel by viewModels()
    private val appsViewModel : AppDrawerViewModel by viewModels()
    private val backupViewModel : BackupViewModel by viewModels()
    private val workspaceViewModel : WorkspaceViewModel by viewModels()

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

            val launchAppColor by ColorSettingsStore.getLaunchAppColor(ctx).collectAsState(initial = null)
            val openUrlColor by ColorSettingsStore.getOpenUrlColor(ctx).collectAsState(initial = null)
            val notificationShadeColor by ColorSettingsStore.getNotificationShadeColor(ctx).collectAsState(initial = null)
            val controlPanelColor by ColorSettingsStore.getControlPanelColor(ctx).collectAsState(initial = null)
            val openAppDrawerColor by ColorSettingsStore.getOpenAppDrawerColor(ctx).collectAsState(initial = null)
            val launcherSettingsColor by ColorSettingsStore.getLauncherSettingsColor(ctx).collectAsState(initial = null)
            val lockColor by ColorSettingsStore.getLockColor(ctx).collectAsState(initial = null)
            val openFileColor by ColorSettingsStore.getOpenFileColor(ctx).collectAsState(initial = null)
            val reloadColor by ColorSettingsStore.getReloadColor(ctx).collectAsState(initial = null)


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

                customLaunchAppColor = launchAppColor,
                customOpenUrlColor = openUrlColor,
                customNotificationShadeColor = notificationShadeColor,
                customControlPanelColor = controlPanelColor,
                customOpenAppDrawerColor = openAppDrawerColor,
                customLauncherSettingsColor = launcherSettingsColor,
                customLockColor = lockColor,
                customOpenFileColor = openFileColor,
                customReloadAppsColor = reloadColor
            ) {

                val navController = rememberNavController()
                navControllerHolder.value = navController

                MainAppUi(
                    backupViewModel = backupViewModel,
                    appViewModel = appsViewModel,
                    workspaceViewModel = workspaceViewModel,
                    navController = navController
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appLifecycleViewModel.onPause()
    }

    override fun onResume() {
        super.onResume()

        val currentRoute = navControllerHolder.value
            ?.currentBackStackEntry
            ?.destination
            ?.route

        // If user was outside > 10s, and not in backup screen, go to MAIN
        if (appLifecycleViewModel.resume(10_000) && currentRoute != SETTINGS.BACKUP) {
            navControllerHolder.value?.navigate(ROUTES.MAIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
