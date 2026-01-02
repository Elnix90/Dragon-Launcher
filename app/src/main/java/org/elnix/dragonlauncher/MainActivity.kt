package org.elnix.dragonlauncher

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.MainAppUi
import org.elnix.dragonlauncher.ui.ROUTES
import org.elnix.dragonlauncher.ui.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.utils.SettingsBackupManager
import org.elnix.dragonlauncher.utils.ignoredReturnRoutes
import org.elnix.dragonlauncher.utils.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.utils.models.AppsViewModel
import org.elnix.dragonlauncher.utils.models.BackupViewModel
import org.elnix.dragonlauncher.utils.models.FloatingAppsViewModel
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel
import org.elnix.dragonlauncher.utils.showToast
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val appLifecycleViewModel : AppLifecycleViewModel by viewModels()
    private val appsViewModel : AppsViewModel by viewModels()
    private val backupViewModel : BackupViewModel by viewModels()
    private val workspaceViewModel : WorkspaceViewModel by viewModels()
    private val floatingAppsViewModel : FloatingAppsViewModel by viewModels()

    private var navControllerHolder = mutableStateOf<NavHostController?>(null)



    companion object {
        private var GLOBAL_APPWIDGET_HOST: AppWidgetHost? = null
    }


    val appWidgetHost: AppWidgetHost by lazy {
        GLOBAL_APPWIDGET_HOST ?: AppWidgetHost(this, R.id.appwidget_host_id).also {
            GLOBAL_APPWIDGET_HOST = it
        }
    }

    private val appWidgetManager by lazy {
        AppWidgetManager.getInstance(this)
    }


    private var pendingBindWidgetId: Int? = null
    private var pendingBindProvider: ComponentName? = null


    private val widgetPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("WIDGET", "widgetPickerLauncher resultCode=${result.resultCode}")
            val widgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                ?: return@registerForActivityResult

            Log.d("WIDGET", "Picked widgetId=$widgetId")
            val info = appWidgetManager.getAppWidgetInfo(widgetId)
            if (info == null) {
                Log.w("WIDGET", "No AppWidgetInfo for widgetId=$widgetId, deleting...")
                appWidgetHost.deleteAppWidgetId(widgetId)
                return@registerForActivityResult
            }

            // Try to bind silently
            if (appWidgetManager.bindAppWidgetIdIfAllowed(widgetId, info.provider)) {
                Log.d("WIDGET", "bindAppWidgetIdIfAllowed=true, proceeding")
                proceedAfterBind(widgetId, info)
            } else {
                Log.d("WIDGET", "bindAppWidgetIdIfAllowed=false, launching bind consent")
                pendingBindWidgetId = widgetId
                pendingBindProvider = info.provider

                val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, Bundle())
                }
                widgetBindLauncher.launch(bindIntent)
            }
        }

    private val widgetBindLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            val widgetId = pendingBindWidgetId
            val provider = pendingBindProvider

            if (widgetId == null || provider == null) return@registerForActivityResult

            pendingBindWidgetId = null
            pendingBindProvider = null

            lifecycleScope.launch {
                // Wait a short time for system to finish binding
                var bound = false
                repeat(5) {
                    val info = try {
                        appWidgetManager.getAppWidgetInfo(widgetId)
                    } catch (e: SecurityException) {
                        Log.w("WIDGET", "Cannot get AppWidgetInfo for widgetId=$widgetId: ${e.message}")
                        null
                    }

                    if (info != null) {
                        bound = true
                        return@repeat
                    }

                    kotlinx.coroutines.delay(200)
                }

                if (bound) {
                    Log.d("WIDGET", "Widget successfully bound after consent: $widgetId")
                    appWidgetManager.getAppWidgetInfo(widgetId)?.let { info ->
                        proceedAfterBind(widgetId, info)
                    } ?: run {
                        Log.w("WIDGET", "No AppWidgetInfo after bind, deleting $widgetId")
                        appWidgetHost.deleteAppWidgetId(widgetId)
                    }
                } else {
                    Log.w("WIDGET", "Widget bind failed or dismissed: $widgetId")
                    appWidgetHost.deleteAppWidgetId(widgetId)
                }
            }
        }


    /**
     * I struggled so much to achieve to something that works in most cases I don't want to change that
     */
    private fun proceedAfterBind(widgetId: Int, info: AppWidgetProviderInfo) {
        Log.d("WIDGET", "proceedAfterBind for widgetId=$widgetId, provider=${info.provider}")
        if (info.configure != null) {

            Log.d("WIDGET", "Widget requires configuration, launching configure activity")
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }


            try {
                widgetConfigureLauncher.launch(intent)
            } catch (e: Exception) {
                Log.w("WIDGET", "Failed to launch configure activity: ${e.message}")
                this.showToast("Failed to launch configure activity: ${e.message}")
                floatingAppsViewModel.addFloatingApp(SwipeActionSerializable.OpenWidget(widgetId,info.provider), info)
            }

        } else {
            Log.d("WIDGET", "No configuration needed, adding widget")
            floatingAppsViewModel.addFloatingApp(SwipeActionSerializable.OpenWidget(widgetId,info.provider), info)
        }
    }

    private val widgetConfigureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val widgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                ?: return@registerForActivityResult

            val info = appWidgetManager.getAppWidgetInfo(widgetId) ?: run {
                // Widget not bound - clean up ID
                Log.w("WidgetsViewModel", "Cannot find info for widgetId: $widgetId")
                return@registerForActivityResult
            }


            if (result.resultCode == RESULT_OK) {
                floatingAppsViewModel.addFloatingApp(SwipeActionSerializable.OpenWidget(widgetId,info.provider), info)
            } else {
                Log.w("WIDGET", "Widget configure canceled, deleting $widgetId")
                appWidgetHost.deleteAppWidgetId(widgetId)
            }
        }

    fun launchWidgetPicker() {
        val widgetId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, Bundle())
            putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, ArrayList())
        }
        widgetPickerLauncher.launch(pickIntent)
    }

    /**
     * Deletes a widget ID and removes it from the host.
     */
    fun deleteWidget(widgetId: Int) {
        appWidgetHost.deleteAppWidgetId(widgetId)
    }



    private val packageReceiver = PackageReceiver()
    private val filter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addDataScheme("package")
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageReceiver, filter, RECEIVER_NOT_EXPORTED)
        }

        // Use hardware acceleration
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        super.onCreate(savedInstanceState)

        appWidgetHost.startListening()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val ctx = LocalContext.current

            // May be used in the future for some quit action / operation
//            DoubleBackToExit()


            val keepScreenOn by BehaviorSettingsStore.getKeepScreenOn(ctx).collectAsState(false)
            val fullscreen by UiSettingsStore.getFullscreen(ctx).collectAsState(false)
            val hasInitialized by PrivateSettingsStore.getHasInitialized(ctx).collectAsState(initial = true)


            val window = this@MainActivity.window
            val controller = WindowInsetsControllerCompat(window, window.decorView)

            LaunchedEffect(keepScreenOn) {
                if (keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            LaunchedEffect(fullscreen) {
                if (fullscreen) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }


            LaunchedEffect(hasInitialized) {
                if (!hasInitialized) {
                    SwipeSettingsStore.savePoints(ctx,
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
            val openRecentAppsColor by ColorSettingsStore.getOpenRecentApps(ctx).collectAsState(initial = null)
            val openCircleNestColor by ColorSettingsStore.getOpenCircleNest(ctx).collectAsState(initial = null)
            val goParentCircleColor by ColorSettingsStore.getGoParentNest(ctx).collectAsState(initial = null)



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
                customReloadAppsColor = reloadColor,
                customOpenRecentAppsColor = openRecentAppsColor,
                customOpenCircleNest = openCircleNestColor,
                customGoParentNest = goParentCircleColor
            ) {

                val navController = rememberNavController()
                navControllerHolder.value = navController

                MainAppUi(
                    backupViewModel = backupViewModel,
                    appsViewModel = appsViewModel,
                    workspaceViewModel = workspaceViewModel,
                    floatingAppsViewModel = floatingAppsViewModel,
                    navController = navController,
                    onLaunchSystemWidgetPicker = { (ctx as MainActivity).launchWidgetPicker() },
                    onResetWidgetSize = { id, widgetId ->
                        val info = appWidgetManager.getAppWidgetInfo(widgetId)
                        floatingAppsViewModel.resetFloatingAppSize(id, info)
                    }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appLifecycleViewModel.onPause()
        lifecycleScope.launch {
            SettingsBackupManager.triggerBackup(this@MainActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            SettingsBackupManager.triggerBackup(this@MainActivity)
        }

        val currentRoute = navControllerHolder.value
            ?.currentBackStackEntry
            ?.destination
            ?.route

        // If user was outside > 10s, and not in the ignored list
        if (appLifecycleViewModel.resume(10_000) && currentRoute !in ignoredReturnRoutes) {
            navControllerHolder.value?.navigate(ROUTES.MAIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageReceiver)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        lifecycleScope.launch {
            SettingsBackupManager.triggerBackup(this@MainActivity)
        }

        // Widgets
        GLOBAL_APPWIDGET_HOST = null
    }


    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

}
