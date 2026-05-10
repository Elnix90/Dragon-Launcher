package org.elnix.dragonlauncher.activity

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.PRIVATE_SPACE_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.STARTUP_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.WIDGET_TAG
import org.elnix.dragonlauncher.common.messyfolder.SamsungWorkspaceIntegration
import org.elnix.dragonlauncher.common.messyfolder.WidgetHostProvider
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.utils.PrivateSpaceUtils
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.models.DragonLogViewModel
import org.elnix.dragonlauncher.models.FloatingAppsViewModel
import org.elnix.dragonlauncher.models.PrivateSpaceViewModel
import org.elnix.dragonlauncher.models.ShizukuViewModel
import org.elnix.dragonlauncher.receiver.BootReceiver
import org.elnix.dragonlauncher.receiver.FontReceiver
import org.elnix.dragonlauncher.receiver.PackageReceiver
import org.elnix.dragonlauncher.settings.SettingsBackupManager
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.ui.MainAppUi
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.composition.LocalAppLifecycleViewModel
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalBackupViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDragonLogViewModel
import org.elnix.dragonlauncher.ui.composition.LocalFloatingAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalPrivateSpaceViewModel
import org.elnix.dragonlauncher.ui.composition.LocalShizukuViewModel
import org.elnix.dragonlauncher.ui.dialogs.CrashScreen
import org.elnix.dragonlauncher.ui.widgets.LauncherWidgetHolder
import java.util.UUID

@AndroidEntryPoint
class MainActivity : FragmentActivity(), WidgetHostProvider {

    private val appLifecycleViewModel: AppLifecycleViewModel by viewModels()
    private val privateSpaceViewModel: PrivateSpaceViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()
    private val floatingAppsViewModel: FloatingAppsViewModel by viewModels()
    private val dragonLogViewModel: DragonLogViewModel by viewModels()
    private val shizukuViewModel: ShizukuViewModel by viewModels()
    private val appsViewModel: AppsViewModel by viewModels()

    companion object {
        private var GLOBAL_APPWIDGET_HOST: AppWidgetHost? = null
        private const val REQUEST_WIDGET_CONFIG = 1001

        private var offScreenTimeout: Int? = null
    }


    val appWidgetHost: AppWidgetHost by lazy {
        GLOBAL_APPWIDGET_HOST ?: AppWidgetHost(this, R.id.appwidget_host_id).also {
            GLOBAL_APPWIDGET_HOST = it
        }
    }

    private val widgetHolder by lazy { LauncherWidgetHolder.getInstance(this) }

    override fun createAppWidgetView(widgetId: Int): AppWidgetHostView? {
        val info = getAppWidgetInfo(widgetId) ?: return null
        return widgetHolder.createView(widgetId, info)
    }

    override fun getAppWidgetInfo(widgetId: Int): AppWidgetProviderInfo? {
        return widgetHolder.getAppWidgetInfo(widgetId)
    }

    private val appWidgetManager by lazy {
        AppWidgetManager.getInstance(this)
    }


    private var pendingBindWidgetId: Int? = null
    private var pendingAddNestId: Int? = null
    private var pendingBindProvider: ComponentName? = null
    private var pendingConfigWidgetId: Int = -1


    fun bindWidgetFromCustomPicker(
        widgetId: Int,
        provider: ComponentName
    ) {
        logD(WIDGET_TAG) { "DRAGON_FLOW: Starting bind process from picker for ID $widgetId" }
        lifecycleScope.launch {
            pendingBindWidgetId = widgetId
            pendingBindProvider = provider
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
            }
            widgetBindLauncher.launch(intent)
        }
    }


    private val widgetBindLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val widgetId = pendingBindWidgetId
            val provider = pendingBindProvider

            logD(WIDGET_TAG) { "DRAGON_FLOW: ActionBind finished with resultCode=${result.resultCode} for ID $widgetId" }

            if (widgetId == null || provider == null) {
                logW(WIDGET_TAG) { "DRAGON_FLOW: Pending data lost during activity transition!" }
                return@registerForActivityResult
            }

            pendingBindWidgetId = null
            pendingBindProvider = null

            lifecycleScope.launch {
                logD(WIDGET_TAG) { "DRAGON_FLOW: Waiting for OS to sync bind state..." }
                // Wait a short time for system to finish binding
                var bound = false
                repeat(5) { attempt ->
                    val info = try {
                        widgetHolder.getAppWidgetInfo(widgetId)
                    } catch (e: SecurityException) {
                        logE(WIDGET_TAG, e) {
                            "DRAGON_FLOW: SecurityException on attempt $attempt for ID $widgetId"
                        }
                        null
                    }

                    if (info != null) {
                        logD(WIDGET_TAG) { "DRAGON_FLOW: Sync successful on attempt $attempt! Info found." }
                        bound = true
                        return@repeat
                    }

                    delay(300)
                }

                if (bound) {
                    logD(WIDGET_TAG) { "DRAGON_FLOW: Widget bound after consent. Proceeding..." }
                    widgetHolder.getAppWidgetInfo(widgetId)?.let { info ->
                        proceedAfterBind(widgetId, info)
                    } ?: run {
                        logW(WIDGET_TAG) { "DRAGON_FLOW: Critical - Info missing for bound ID $widgetId" }
                        widgetHolder.deleteAppWidgetId(widgetId)
                    }
                } else {
                    logW(WIDGET_TAG) { "DRAGON_FLOW: Bind FAILED after consent. ID $widgetId was not blessed by system." }
                    showToast("Binding failed. Check Xiaomi 'Add Shortcut' permission.")
                    widgetHolder.deleteAppWidgetId(widgetId)
                }
            }
        }


    /**
     * I struggled so much to achieve to something that works in most cases I don't want to change that
     */
    private fun proceedAfterBind(widgetId: Int, info: AppWidgetProviderInfo) {
        logD(WIDGET_TAG) { "DRAGON_FLOW: proceedAfterBind for ID $widgetId, provider=${info.provider}" }

        if (info.configure != null) {
            logD(WIDGET_TAG) { "DRAGON_FLOW: Provider requires configuration. Launching via Host Proxy..." }
            pendingConfigWidgetId = widgetId
            try {
                // Use the official AppWidgetHost proxy to launch configuration.
                // This is REQUIRED for widgets with non-exported configuration activities (like GitHub).
                appWidgetHost.startAppWidgetConfigureActivityForResult(
                    this,
                    widgetId,
                    0,
                    REQUEST_WIDGET_CONFIG,
                    null
                )
            } catch (e: Exception) {
                logE(WIDGET_TAG, e) { "DRAGON_FLOW: Proxy launch failed" }
                showToast("Failed to launch configuration")
                // Add it anyway if config fails to launch
                floatingAppsViewModel.addFloatingApp(
                    action = SwipeActionSerializable.OpenWidget(
                        widgetId,
                        info.provider.packageName,
                        info.provider.className
                    ),
                    info = info,
                    nestId = pendingAddNestId ?: 0
                )
                pendingAddNestId = null
            }
        } else {
            logD(WIDGET_TAG) { "DRAGON_FLOW: No configuration needed, adding widget" }
            floatingAppsViewModel.addFloatingApp(
                action = SwipeActionSerializable.OpenWidget(
                    widgetId,
                    info.provider.packageName,
                    info.provider.className
                ),
                info = info,
                nestId = pendingAddNestId ?: 0
            )
            pendingAddNestId = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_WIDGET_CONFIG) {
            val widgetId =
                data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingConfigWidgetId)
                    ?: pendingConfigWidgetId
            logD(WIDGET_TAG) { "DRAGON_FLOW: Proxy config finished for ID $widgetId, result=$resultCode" }

            if (resultCode == RESULT_OK && widgetId != -1) {
                val info = widgetHolder.getAppWidgetInfo(widgetId)
                if (info != null) {
                    floatingAppsViewModel.addFloatingApp(
                        action = SwipeActionSerializable.OpenWidget(
                            widgetId,
                            info.provider.packageName,
                            info.provider.className
                        ),
                        info = info,
                        nestId = pendingAddNestId ?: 0
                    )
                }
            } else if (widgetId != -1) {
                // User canceled or config failed, clean up the ID
                widgetHolder.deleteAppWidgetId(widgetId)
            }
            pendingConfigWidgetId = -1
            pendingAddNestId = null
        }
    }

    /**
     * Deletes a widget ID and removes it from the host.
     */
    fun deleteWidget(widgetId: Int) {
        widgetHolder.deleteAppWidgetId(widgetId)
    }

    private val packageReceiver = PackageReceiver()
    private val fontsReceiver = FontReceiver()
    private val bootReceiver = BootReceiver()

    private val filter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addAction(Intent.ACTION_PACKAGE_REPLACED)
        addAction(Intent.ACTION_PACKAGES_SUSPENDED)
        addAction(Intent.ACTION_PACKAGES_UNSUSPENDED)
        addAction(Intent.ACTION_PACKAGE_CHANGED)
        addDataScheme("package")
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        val startTime = System.currentTimeMillis()
        // Use hardware acceleration ASAP
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        super.onCreate(savedInstanceState)
        logI(STARTUP_TAG) { "MainActivity.onCreate started" }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                registerReceiver(packageReceiver, filter, RECEIVER_EXPORTED)
            } catch (e: Exception) {
                logE(TAG, e) { "Failed to register packageReceiver" }
            }

            // Register fonts update receiver (extensions send org.elnix.dragonlauncher.ACTION_FONTS_RESULT)
            try {
                registerReceiver(fontsReceiver, IntentFilter("org.elnix.dragonlauncher.ACTION_FONTS_RESULT"), RECEIVER_EXPORTED)
            } catch (e: Exception) {
                logE(TAG, e) { "Failed to register fontsReceiver" }
            }

            try {
                registerReceiver(bootReceiver, IntentFilter(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED), RECEIVER_EXPORTED)
            } catch (e: Exception) {
                logE(TAG, e) { "Failed to register bootReceiver" }
            }
        }

        appWidgetHost.startListening()

        var lastStackTrace by mutableStateOf(runBlocking {
            PrivateSettingsStore.lastCrashStackTrace.getOrNull(this@MainActivity)
        })

        enableEdgeToEdge()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        setContent {

            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()

            if (lastStackTrace.isNullOrBlank()) {

                DragonLauncherTheme {

                    // Force launch of full viewmodel after first frame for performance
                    // This avoids layout & loading overlap
                    LaunchedEffect(Unit) {
                        lifecycleScope.launch(Dispatchers.Default) {
                            yield() // Wait for first frame
                            logI(STARTUP_TAG) {
                                "First frame rendered in ${System.currentTimeMillis() - startTime}ms. Starting AppsViewModel.loadAll()."
                            }
                            appsViewModel.loadAll()
                            logI(STARTUP_TAG) {
                                "AppsViewModel.loadAll() finished at ${System.currentTimeMillis() - startTime}ms total."
                            }


                            // All stores excepted the non-backupable ones, cause they trigger updates constantly (e.g., last backup time)
                            backupableStores.forEach { (_, store) ->
                                store.onAnySettingChanged = {
                                    // Schedule backup using the Settings backup manager
                                    lifecycleScope.launch {
                                        SettingsBackupManager.triggerBackup(this@MainActivity)
                                    }
                                }
                            }
                        }
                    }


                    val lifecycleOwner = LocalLifecycleOwner.current


                    // May be used in the future for some quit action / operation
                    // DoubleBackToExit()

                    // Used to visually block private space content on window quit, and if user locks his phone,
                    // the apps are also visually blocked, since they can't be launched
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (
                                event == Lifecycle.Event.ON_RESUME &&
                                PrivateSpaceUtils.isPrivateSpaceSupported()
                            ) {
                                val locked = PrivateSpaceUtils.isPrivateSpaceLocked(ctx) ?: false

                                // If private space is locked on return, set it unavailable on the viewmodel state
                                if (locked) {
                                    appsViewModel.setPrivateSpaceLocked()
                                } else { // Set it available
                                    scope.launch(Dispatchers.IO) {
                                        appsViewModel.unlockAndReloadPrivateSpace()
                                    }
                                }
                            }
                        }

                        // Add the observer to the lifecycle
                        lifecycleOwner.lifecycle.addObserver(observer)

                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }


                    val keepScreenOn by BehaviorSettingsStore.keepScreenOn.asState()
                    val fullscreen by UiSettingsStore.fullScreen.asState()
                    val hasInitialized by PrivateSettingsStore.hasInitialized.asStateNull()
                    val samsungPreferSecureFolder by PrivateSettingsStore.samsungPreferSecureFolder.asState()


                    val offScreenTimeout by BehaviorSettingsStore.offScreenTimeout.asState()
                    LaunchedEffect(offScreenTimeout) {
                        Companion.offScreenTimeout = offScreenTimeout
                    }


                    LaunchedEffect(Unit) {
                        privateSpaceViewModel.privateSpaceUnlockRequestEvents.collect {

                            val openPrivateSpace = {
                                logI(PRIVATE_SPACE_TAG) { "Using standard Android Private Space" }
                                ctx.startActivity(
                                    Intent(ctx, PrivateSpaceUnlockActivity::class.java)
                                )
                            }

                            logI(PRIVATE_SPACE_TAG) { "Loading Samsung preference: $samsungPreferSecureFolder" }
                            val useSecureFolder = SamsungWorkspaceIntegration.resolveUseSecureFolder(
                                ctx = ctx,
                                preferenceEnabled = samsungPreferSecureFolder
                            )
                            logI(PRIVATE_SPACE_TAG) { "Using system: ${if (useSecureFolder) "Secure Folder" else "Private Space"}" }

                            if (useSecureFolder) {
                                SamsungWorkspaceIntegration.openSecureFolder(
                                    ctx = ctx,
                                    onFallback = openPrivateSpace
                                )
                            } else {
                                openPrivateSpace()
                            }
                        }
                    }


                    val window = this@MainActivity.window
                    val controller = WindowInsetsControllerCompat(window, window.decorView)

                    LaunchedEffect(keepScreenOn) {
                        if (keepScreenOn) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }

                    LaunchedEffect(Unit, fullscreen) {
                        if (fullscreen) {
                            controller.hide(WindowInsetsCompat.Type.systemBars())
                            controller.systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        } else {
                            controller.show(WindowInsetsCompat.Type.systemBars())
                        }
                    }


                    LaunchedEffect(hasInitialized) {
                        if (hasInitialized == false) {

                            /* ───────────── Create the 3 default points (has to be changed ───────────── */
                            SwipeSettingsStore.savePoints(
                                ctx,
                                listOf(
                                    SwipePointSerializable(
                                        circleNumber = 0,
                                        angleDeg = 0.toDouble(),
                                        action = SwipeActionSerializable.OpenAppDrawer(),
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

                            /* ───────────── Finally, initialize ───────────── */
                            PrivateSettingsStore.hasInitialized.set(ctx, true)
                        }
                    }


                    CompositionLocalProvider(
                        LocalBackupViewModel provides backupViewModel,
                        LocalAppsViewModel provides appsViewModel,
                        LocalAppLifecycleViewModel provides appLifecycleViewModel,
                        LocalPrivateSpaceViewModel provides privateSpaceViewModel,
                        LocalFloatingAppsViewModel provides floatingAppsViewModel,
                        LocalDragonLogViewModel provides dragonLogViewModel,
                        LocalShizukuViewModel provides shizukuViewModel
                    ) {
                        MainAppUi(
                            onBindCustomWidget = { widgetId, provider, nestId ->
                                pendingAddNestId = nestId
                                (ctx as MainActivity).bindWidgetFromCustomPicker(widgetId, provider)
                            },
                            onResetWidgetSize = { id, widgetId ->
                                val info = appWidgetManager.getAppWidgetInfo(widgetId)
                                floatingAppsViewModel.resetFloatingAppSize(id, info)
                            },
                            onRemoveFloatingApp = { floatingAppObject ->
                                floatingAppsViewModel.removeFloatingApp(floatingAppObject.id) {
                                    (ctx as MainActivity).deleteWidget(it)
                                }
                            }
                        )
                    }
                }
            } else {
                MaterialTheme {
                    CrashScreen(
                        stackTrace = lastStackTrace ?: "Unable to recover last stackTrace",
                        onDismiss = {
                            scope.launch {
                                PrivateSettingsStore.lastCrashStackTrace.reset(ctx)
                            }
                            lastStackTrace = null
                        }
                    )
                }
            }
        }
    }


    // For the home action, to prevent it to work TOO MUCH (I tested, and it was launching
    // the action everytime I clicked on the home button/gesture lol
    var pauseTime: Long = 0L
    var isNewHomeIntent: Boolean = false


//    override fun onPause() {
//        super.onPause()
//
//        /* ────────────────  Home detection actions ──────────────── */
//        pauseTime = SystemClock.uptimeMillis()
//
//
//        /* ──────────────── Returns back to home if outside for too long ─────────────────── */
//        appLifecycleViewModel.onPause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//
//        /* ────────────────  Home detection actions ──────────────── */
//        val now = SystemClock.uptimeMillis()
//        val delta = now - pauseTime
//
//        if (
//            isNewHomeIntent &&
//            delta in 1..HOME_REENTER_WINDOW_MS
//        ) {
//            // HOME pressed while launcher already visible
//            isNewHomeIntent = false
//            appLifecycleViewModel.onHomeAction()
//        }
//
//        isNewHomeIntent = false
//    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)


        /* Detects if the new Intent is the launcher one, and set the pending value to true */
        if (
            intent.action == Intent.ACTION_MAIN &&
            intent.hasCategory(Intent.CATEGORY_HOME)
        ) {
            appLifecycleViewModel.onHomeAction()
            logD(TAG) { "HOME intent received (pending)" }
        }
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }


    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageReceiver)
        } catch (_: Exception) {
        }
        try {
            unregisterReceiver(fontsReceiver)
        } catch (_: Exception) {
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        lifecycleScope.launch {
            SettingsBackupManager.triggerBackup(this@MainActivity)
        }

        // Widgets
        GLOBAL_APPWIDGET_HOST = null
    }
}
