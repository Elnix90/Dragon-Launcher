package org.elnix.dragonlauncher.ui

import android.Manifest
import android.R.attr.versionCode
import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.navigaton.EDIT_SCREENS
import org.elnix.dragonlauncher.common.navigaton.ROUTES
import org.elnix.dragonlauncher.common.navigaton.SETTINGS
import org.elnix.dragonlauncher.common.navigaton.homeRoutes
import org.elnix.dragonlauncher.common.serializables.ColorSerializer
import org.elnix.dragonlauncher.common.serializables.FloatingAppObject
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.StatusBarJson
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.dummySwipePoint
import org.elnix.dragonlauncher.common.serializables.allShapesWithoutRandom
import org.elnix.dragonlauncher.common.utils.Constants
import org.elnix.dragonlauncher.common.utils.Constants.Logging.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.ICONS_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.NAVIGATION_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.SHIZUKU_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.STATUS_BAR_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.utils.Constants.Navigation.transparentScreens
import org.elnix.dragonlauncher.common.utils.Constants.PackageNames.SHIZUKU_PACKAGE_NAME
import org.elnix.dragonlauncher.common.utils.Constants.URLs.URL_SHIZUKU_SITE
import org.elnix.dragonlauncher.common.utils.getVersionCode
import org.elnix.dragonlauncher.common.utils.hasUriReadWritePermission
import org.elnix.dragonlauncher.common.utils.isAppInstalled
import org.elnix.dragonlauncher.common.utils.isDefaultLauncher
import org.elnix.dragonlauncher.common.utils.loadChangelogs
import org.elnix.dragonlauncher.common.utils.openUrl
import org.elnix.dragonlauncher.common.utils.showToast
import org.elnix.dragonlauncher.enumsui.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.LockMethod
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logV
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.settings.stores.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.WellbeingSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppLaunchException
import org.elnix.dragonlauncher.ui.actions.launchAppDirectly
import org.elnix.dragonlauncher.ui.actions.launchSwipeAction
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalAppItemSettings
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.base.compositionslocals.rememberAppItemSettings
import org.elnix.dragonlauncher.ui.base.overlays.OverlayHost
import org.elnix.dragonlauncher.ui.composition.LocalAngleLineObject
import org.elnix.dragonlauncher.ui.composition.LocalAppLifecycleViewModel
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalBackupViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.composition.LocalDrawerIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalEndLineObject
import org.elnix.dragonlauncher.ui.composition.LocalHoldCustomObject
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.composition.LocalLineObject
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.composition.LocalNavController
import org.elnix.dragonlauncher.ui.composition.LocalNests
import org.elnix.dragonlauncher.ui.composition.LocalPointIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalPoints
import org.elnix.dragonlauncher.ui.composition.LocalShizukuViewModel
import org.elnix.dragonlauncher.ui.composition.LocalShowLabelsInAddPointDialog
import org.elnix.dragonlauncher.ui.composition.LocalStartLineObject
import org.elnix.dragonlauncher.ui.composition.LocalStatusBarElements
import org.elnix.dragonlauncher.ui.dialogs.AdbCommandInputDialog
import org.elnix.dragonlauncher.ui.dialogs.FilePickerDialog
import org.elnix.dragonlauncher.ui.dialogs.GoogleLockingWarning
import org.elnix.dragonlauncher.ui.dialogs.MainScreeLayersTab
import org.elnix.dragonlauncher.ui.dialogs.PinUnlockDialog
import org.elnix.dragonlauncher.ui.dialogs.ShizukuOutputDialog
import org.elnix.dragonlauncher.ui.dialogs.ShizukuUnavailableDialog
import org.elnix.dragonlauncher.ui.dialogs.WidgetPickerDialog
import org.elnix.dragonlauncher.ui.dialogs.rememberMainScreenLayerOrder
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.drawer.AppDrawerScreen
import org.elnix.dragonlauncher.ui.helpers.FpsCounterGraph
import org.elnix.dragonlauncher.ui.helpers.LauncherSnackbarHost
import org.elnix.dragonlauncher.ui.helpers.PrivateSpaceStateDebugScreen
import org.elnix.dragonlauncher.ui.helpers.ReselectAutoBackupBanner
import org.elnix.dragonlauncher.ui.helpers.SecurityHelper
import org.elnix.dragonlauncher.ui.helpers.SetDefaultLauncherBanner
import org.elnix.dragonlauncher.ui.helpers.findFragmentActivity
import org.elnix.dragonlauncher.ui.navigation.collapseDownAnimation
import org.elnix.dragonlauncher.ui.navigation.raiseUpAnimation
import org.elnix.dragonlauncher.ui.navigation.settingComposable
import org.elnix.dragonlauncher.ui.remembers.rememberDecodedObject
import org.elnix.dragonlauncher.ui.settings.backup.BackupTab
import org.elnix.dragonlauncher.ui.settings.customization.AngleLineTab
import org.elnix.dragonlauncher.ui.settings.customization.AppearanceTab
import org.elnix.dragonlauncher.ui.settings.customization.BehaviorTab
import org.elnix.dragonlauncher.ui.settings.customization.ColorSelectorTab
import org.elnix.dragonlauncher.ui.settings.customization.DrawerTab
import org.elnix.dragonlauncher.ui.settings.customization.FloatingAppsTab
import org.elnix.dragonlauncher.ui.settings.customization.FontTab
import org.elnix.dragonlauncher.ui.settings.customization.HoldToActivateArcTab
import org.elnix.dragonlauncher.ui.settings.customization.IconPackTab
import org.elnix.dragonlauncher.ui.settings.customization.NestEditingScreen
import org.elnix.dragonlauncher.ui.settings.customization.StatusBarTab
import org.elnix.dragonlauncher.ui.settings.customization.ThemesTab
import org.elnix.dragonlauncher.ui.settings.customization.WallpaperTab
import org.elnix.dragonlauncher.ui.settings.debug.DebugTab
import org.elnix.dragonlauncher.ui.settings.debug.LogsTab
import org.elnix.dragonlauncher.ui.settings.debug.LogsViewerScreen
import org.elnix.dragonlauncher.ui.settings.debug.SettingsDebugTab
import org.elnix.dragonlauncher.ui.settings.extensions.ExtensionsTab
import org.elnix.dragonlauncher.ui.settings.language.LanguageTab
import org.elnix.dragonlauncher.ui.settings.wellbeing.WellbeingTab
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceDetailScreen
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceListScreen
import org.elnix.dragonlauncher.ui.welcome.WelcomeScreen
import org.elnix.dragonlauncher.ui.wellbeing.AppTimerService
import org.elnix.dragonlauncher.ui.wellbeing.DigitalPauseActivity
import org.elnix.dragonlauncher.ui.whatsnew.ChangelogsScreen
import org.elnix.dragonlauncher.ui.whatsnew.WhatsNewBottomSheet
import rikka.shizuku.Shizuku


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MainAppUi(
    onBindCustomWidget: (Int, ComponentName, nestId: Int) -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit,
    onRemoveFloatingApp: (FloatingAppObject) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val appsViewModel = LocalAppsViewModel.current
    val appLifecycleViewModel = LocalAppLifecycleViewModel.current
    val backupViewModel = LocalBackupViewModel.current

    val shizukuViewModel = LocalShizukuViewModel.current
    val shizukuOutput by shizukuViewModel.outputValue.collectAsState()

    val result by backupViewModel.result.collectAsState()

    val privateSpaceState = appsViewModel.privateSpaceState

    /* ───────────── Changelogs system ───────────── */
    val lastSeenVersionCodeWhatsNew by PrivateSettingsStore.lastSeenVersionCodeWhatsNew.asState()
    val lastSeenVersionCodeGoogleLockdownWarning by PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.asState()

    val currentVersionCode = ctx.getVersionCode()
    var showWhatsNewBottomSheet by remember { mutableStateOf(false) }

    var showShizukuCommandPromter by remember { mutableStateOf<SwipeActionSerializable.RunAdbCommand?>(null) }

    var showWidgetPicker by remember { mutableStateOf<Int?>(null) }
    var showFilePicker: SwipePointSerializable? by remember { mutableStateOf(null) }

    val updates by produceState(initialValue = emptyList()) {
        value = loadChangelogs(ctx, versionCode)
    }


    var showShizukuUnavailableDialog by rememberSaveable { mutableStateOf(false) }
    val hasShizukuPermission by shizukuViewModel.shizukuPermissionState().collectAsState()
    var isShizukuInstalled by rememberSaveable {
        mutableStateOf(
            ctx.isAppInstalled(
                SHIZUKU_PACKAGE_NAME
            )
        )
    }



    val autoShowKeyboardOnDrawer by DrawerSettingsStore.autoShowKeyboardOnDrawer.asState()

    val selectedToolbarItemsStringSet by DrawerSettingsStore.toolbarsOrder.asState()
    val selectedToolbarItems by remember {
        derivedStateOf {
            try {
                selectedToolbarItemsStringSet.split(',').map {
                    DrawerToolbar.valueOf(it)
                }
            } catch (e: Exception) {
                logE(Constants.Logging.DRAWER_TAG, e) { "Unable to decode drawerToolbars order, using default value" }
                DrawerToolbar.entries
            }
        }
    }


    val drawerEnterExitAnimations by DrawerSettingsStore.drawerEnterExitAnimations.asState()


    val homeAction by BehaviorSettingsStore.homeAction.asState()

    val leftDrawerAction by DrawerSettingsStore.leftDrawerAction.asState()
    val rightDrawerAction by DrawerSettingsStore.rightDrawerAction.asState()

    val leftDrawerWidth by DrawerSettingsStore.leftDrawerWidth.asState()
    val rightDrawerWidth by DrawerSettingsStore.rightDrawerWidth.asState()

    val showSetDefaultLauncherBanner by PrivateSettingsStore.showSetDefaultLauncherBanner.asStateNull()

    val hasSeenWelcome by PrivateSettingsStore.hasSeenWelcome.asStateNull()

    val useAccessibilityInsteadOfContextToExpandActionPanel by DebugSettingsStore
        .useAccessibilityInsteadOfContextToExpandActionPanel.asState()

    val disableHapticFeedbackGlobally by BehaviorSettingsStore.disableHapticFeedbackGlobally.asState()

    val lifecycleOwner = LocalLifecycleOwner.current
    var isDefaultLauncher by remember { mutableStateOf(ctx.isDefaultLauncher) }


    val navController = LocalNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var lastRoute by remember { mutableStateOf(ROUTES.MAIN) }


    val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
    val autoBackupUriString by BackupSettingsStore.autoBackupUri.asStateNull()
    val autoBackupUri = autoBackupUriString?.toUri()

    var startDestination by remember { mutableStateOf(SETTINGS.ROOT) }


    val showFps by DebugSettingsStore.showFps.asState()

    /* ───────────── Lock gate state ───────────── */
    val lockMethod by PrivateSettingsStore.lockMethod.asState()
    val pinHash by PrivateSettingsStore.lockPinHash.asState()

    /** Once unlocked during this session, stay unlocked */
    var isUnlocked by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf<String?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(lockMethod) {
        isUnlocked = lockMethod == LockMethod.NONE
    }

    LaunchedEffect(currentRoute) {
        currentRoute?.let {
            lastRoute = it
            if (it in homeRoutes) {
                isUnlocked = false
            }
        }
    }

    /*  ─────────────  Wellbeing Settings  ─────────────  */
    val socialMediaPauseEnabled by WellbeingSettingsStore.socialMediaPauseEnabled.asState()
    val guiltModeEnabled by WellbeingSettingsStore.guiltModeEnabled.asState()
    val pauseDuration by WellbeingSettingsStore.pauseDurationSeconds.asState()
    val pausedApps by WellbeingSettingsStore.pausedApps.asState()
    val reminderEnabled by WellbeingSettingsStore.reminderEnabled.asState()
    val reminderInterval by WellbeingSettingsStore.reminderIntervalMinutes.asState()
    val reminderMode by WellbeingSettingsStore.reminderMode.asState()
    val returnToLauncherEnabled by WellbeingSettingsStore.returnToLauncherEnabled.asState()

    /* ───────────── Store pending package to launch after pause ───────────── */
    var pendingPackageToLaunch by remember { mutableStateOf<String?>(null) }
    var pendingUserIdToLaunch by remember { mutableStateOf<Int?>(null) }
    var pendingAppName by remember { mutableStateOf<String?>(null) }

    val digitalPauseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (pendingPackageToLaunch != null) {
            val packageName = pendingPackageToLaunch!!

            logD(APP_LAUNCH_TAG) { "result: $result" }

            if (result.resultCode == DigitalPauseActivity.RESULT_PROCEED) {
                try {
                    // Start reminder-only timer if enabled (no time limit)
                    if (reminderEnabled) {
                        AppTimerService.start(
                            ctx = ctx,
                            packageName = packageName,
                            appName = pendingAppName ?: packageName,
                            reminderEnabled = true,
                            reminderIntervalMinutes = reminderInterval,
                            reminderMode = reminderMode
                        )
                    }

                    launchAppDirectly(
                        appsViewModel,
                        ctx,
                        packageName,
                        pendingUserIdToLaunch!!
                    )
                } catch (e: Exception) {
                    logE(TAG, e) { "Failed to launch after pause" }
                }
            } else if (result.resultCode == DigitalPauseActivity.RESULT_PROCEED_WITH_TIMER) {
                try {
                    val data = result.data
                    val timeLimitMin =
                        data?.getIntExtra(DigitalPauseActivity.RESULT_EXTRA_TIME_LIMIT, 10) ?: 10
                    val hasReminder =
                        data?.getBooleanExtra(DigitalPauseActivity.EXTRA_REMINDER_ENABLED, false)
                            ?: false
                    val remInterval =
                        data?.getIntExtra(DigitalPauseActivity.EXTRA_REMINDER_INTERVAL, 5) ?: 5
                    val remMode =
                        data?.getStringExtra(DigitalPauseActivity.EXTRA_REMINDER_MODE) ?: "overlay"

                    AppTimerService.start(
                        ctx = ctx,
                        packageName = packageName,
                        appName = pendingAppName ?: packageName,
                        reminderEnabled = hasReminder,
                        reminderIntervalMinutes = remInterval,
                        reminderMode = remMode,
                        timeLimitEnabled = true,
                        timeLimitMinutes = timeLimitMin
                    )

                    launchAppDirectly(
                        appsViewModel,
                        ctx,
                        packageName,
                        pendingUserIdToLaunch!!
                    )
                } catch (e: Exception) {
                    logE(APP_LAUNCH_TAG, e) {
                        "Failed to launch after pause with timer"
                    }
                }
            }
        }
        pendingUserIdToLaunch = null
        pendingPackageToLaunch = null
        pendingAppName = null
    }

    LaunchedEffect(Unit, lastSeenVersionCodeWhatsNew, currentRoute) {
        showWhatsNewBottomSheet =
            lastSeenVersionCodeWhatsNew < currentVersionCode && currentRoute != ROUTES.WELCOME
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // The activity resumes when the user returns from the Home settings screen
            if (event == Lifecycle.Event.ON_RESUME) {
                // IMPORTANT: Re-check the status and update the state
                isDefaultLauncher = ctx.isDefaultLauncher
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the noAnimComposable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    /* ───────────── navigation functions, all settings are nested under the lock state ───────────── */

    fun popBackMainScreen() {
        isUnlocked = false
        navController.popBackStack(ROUTES.MAIN, inclusive = false)
    }

    fun goDrawer() = navController.navigate(ROUTES.DRAWER)
    fun goWelcome() = navController.navigate(ROUTES.WELCOME)

    LaunchedEffect(hasSeenWelcome) {
        if (hasSeenWelcome == false) goWelcome()
    }


    @SuppressLint("LocalContextGetResourceValueCall")
    fun goSettings(route: String, popBackStack: Boolean = true) {

        fun go() {
            if (popBackStack) {
                logD(NAVIGATION_TAG) { "Popping back stack to $route" }
                val popped = navController.popBackStack(route, inclusive = false)
                if (!popped) {
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            } else {
                logD(NAVIGATION_TAG) { "Navigating to $route" }
                navController.navigate(route)
            }
        }

        if (isUnlocked || lockMethod == LockMethod.NONE) {
            go()
            return
        }
        @Suppress("KotlinConstantConditions")
        when (lockMethod) {
            LockMethod.PIN -> {
                showPinDialog = route
            }

            LockMethod.DEVICE_UNLOCK -> {
                val activity = ctx.findFragmentActivity()
                if (activity != null && SecurityHelper.isDeviceUnlockAvailable(ctx)) {
                    SecurityHelper.showDeviceUnlockPrompt(
                        activity = activity,
                        onSuccess = {
                            isUnlocked = true
                            go()
                        },
                        onError = { msg ->
                            ctx.showToast(ctx.getString(R.string.authentication_error, msg))
                        },
                        onFailed = {
                            ctx.showToast(ctx.getString(R.string.authentication_failed))
                        }
                    )
                } else {
                    ctx.showToast(ctx.getString(R.string.device_credentials_not_available))
                }
            }

            LockMethod.NONE -> go()
        }
    }

    fun popBackToSettingsRoot() = goSettings(SETTINGS.ROOT)
    fun popBackToAdvSettingsRoot() = goSettings(SETTINGS.ADVANCED_ROOT)
    fun popBackToAppearance() = goSettings(SETTINGS.APPEARANCE)
    fun popBackToDebug() = goSettings(SETTINGS.DEBUG)


    fun launchWidgetsPicker(nestId: Int) {
        showWidgetPicker = nestId
    }


    fun runShisukuCommandNotEmpty(command: SwipeActionSerializable.RunAdbCommand) {
        if (!Shizuku.pingBinder()) {
            logD(SHIZUKU_TAG) { "Shizuku is not running, opening it..." }

            showShizukuUnavailableDialog = true
            return
        }

        if (!hasShizukuPermission) {
            logD(SHIZUKU_TAG) { "Shizuku his not allowed" }

            shizukuViewModel.requestShizukuPermission()
        } else {
            logD(SHIZUKU_TAG) { "Shizuku tries to run the command: $command" }
            if (command.toast == true) {
                ctx.showToast("Running: $command")
            }
            shizukuViewModel.executeShizukuCommand(command.command)
        }
    }

    fun launchAction(point: SwipePointSerializable) {
        // Store package for potential pause callback
        val action = point.action

        // Store package for potential pause callback
        if (action is SwipeActionSerializable.LaunchApp) {
            pendingPackageToLaunch = action.packageName
            pendingUserIdToLaunch = action.userId ?: 0
            pendingAppName = point.customName ?: try {
                ctx.packageManager.getApplicationLabel(
                    ctx.packageManager.getApplicationInfo(action.packageName, 0)
                ).toString()
            } catch (_: Exception) {
                action.packageName
            }
        }

        try {
            launchSwipeAction(
                ctx = ctx,
                appsViewModel = appsViewModel,
                action = action,
                useAccessibilityInsteadOfContextToExpandActionPanel = useAccessibilityInsteadOfContextToExpandActionPanel,
                pausedApps = pausedApps,
                socialMediaPauseEnabled = socialMediaPauseEnabled,
                guiltModeEnabled = guiltModeEnabled,
                pauseDuration = pauseDuration,
                reminderEnabled = reminderEnabled,
                reminderIntervalMinutes = reminderInterval,
                reminderMode = reminderMode,
                returnToLauncherEnabled = returnToLauncherEnabled,
                appName = pendingAppName ?: "",
                digitalPauseLauncher = digitalPauseLauncher,
                onOpenPrivateSpaceApp = { action ->
                    if (action !is SwipeActionSerializable.LaunchApp) return@launchSwipeAction

                    if (privateSpaceState.value.isLocked) {
                        appLifecycleViewModel.onUnlockPrivateSpace()
                    }

                    scope.launch {

                        logD(APP_LAUNCH_TAG) { "Waiting for private space to unlock before launch" }

                        val unlocked = withTimeoutOrNull(10_000L) {
                            privateSpaceState
                                .filter { !it.isLocked }
                                .first()
                        }

                        if (unlocked != null) {
                            logD(APP_LAUNCH_TAG) { "Private space unlocked, launching" }
                            launchAction(dummySwipePoint(action.copy(isPrivateSpace = false)))
                        } else {
                            logW(APP_LAUNCH_TAG) { "Timeout expired for private space unlock" }
                        }
                    }
                },
                onReloadApps = { scope.launch { appsViewModel.reloadApps() } },
                onReselectFile = { showFilePicker = point },
                onAppSettings = {
                    goSettings(it, false)
                },
                onAppDrawer = { workspaceId ->
                    if (workspaceId != null) {
                        appsViewModel.selectWorkspace(workspaceId)
                    }
                    goDrawer()
                },
                onShizukuCommand = { command ->
                    logD(SHIZUKU_TAG) { "Got shizuku command: $command" }

                    if (command.command.trim().isEmpty()) {
                        showShizukuCommandPromter = command
                    }

                    runShisukuCommandNotEmpty(command)
                }
            )
        } catch (e: AppLaunchException) {
            logE(TAG, e) { "Failed to launch action" }
        } catch (e: Exception) {
            logE(TAG, e) { "Unknow error while launching action" }
        }
    }

    fun launchAction(action: SwipeActionSerializable) {
        launchAction(
            dummySwipePoint(action)
        )
    }


    // Drawer home action receiver
    var drawerHomeHandler by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        appLifecycleViewModel.homeEvents.collect {

            logD(Constants.Logging.DRAWER_TAG) { "Got home event, launching home action, lastRoute: $lastRoute" }
            when (lastRoute) {
                ROUTES.DRAWER -> {
                    drawerHomeHandler?.invoke()
                }

                ROUTES.MAIN -> {
                    launchAction(homeAction)
                }
                ROUTES.WELCOME -> {
                    // Do nothing when in welcome screen
                }

                else -> {
                    popBackMainScreen()
                }
            }
        }
    }

    val showSetAsDefaultBanner = (showSetDefaultLauncherBanner == true) &&
            !isDefaultLauncher &&
            currentRoute != ROUTES.WELCOME


    var hasAutoBackupPermission by remember {
        mutableStateOf<Boolean?>(null)
    }

    LaunchedEffect(autoBackupUri) {
        hasAutoBackupPermission = if (autoBackupUri == null) {
            null
        } else {
            ctx.hasUriReadWritePermission(autoBackupUri)
        }
    }

    val showReselectAutoBackupFile =
        autoBackupEnabled &&
                hasAutoBackupPermission == false &&
                autoBackupUri != null &&
                currentRoute != ROUTES.WELCOME


    val containerColor by animateColorAsState(
        if (currentRoute in transparentScreens) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.background
        }
    )


    /* ───────────── Start Composition locals getters ───────────── */

    val iconsShape by DrawerSettingsStore.iconsShape.asState()

    // Used internally by the app view model
    // Caches the icon shape inside to avoid having to pass the shape through each call of a reload icon
    // Crashes if shape not defined, but as it is passed soon enough, this should be ok (never saw any crash tough)
    LaunchedEffect(iconsShape) {
        appsViewModel.cacheIconShape(iconsShape)
    }

    val nests by SwipeSettingsStore.getNestsFlow(ctx).collectAsState(initial = emptyList())
    val points by SwipeSettingsStore.getPointsFlow(ctx).collectAsState(emptyList())
    val defaultPoint by SwipeSettingsStore.getDefaultPointFlow(ctx)
        .collectAsState(defaultSwipePointsValues)

    val pointsIconCache = appsViewModel.pointsIconsCache
    LaunchedEffect(points.size) {
        logD(ICONS_TAG) { "Updating icons cache size to ${points.size}"}
        pointsIconCache.updateMaxCacheSize(points.size)
    }

    val drawerIconCache = appsViewModel.drawerIconCache

    val colorTestMode by ColorModesSettingsStore.colorTestMode.asState()

    val elementsJson by StatusBarJsonSettingsStore.jsonSetting.asState()

    val elements by remember(elementsJson) {

        derivedStateOf {
            StatusBarJson.decodeStatusBarElements(elementsJson).also {
                logV(STATUS_BAR_TAG) { "Element: $elementsJson, decoded: $it" }
            }
        }
    }

    /* ───────────── Decodes the angle lines things ───────────── */

    val json = Json {
        serializersModule = SerializersModule {
            contextual(Color::class, ColorSerializer)
        }
    }

    val lineJson by AngleLineSettingsStore.lineJson.asState()
    val lineObject = rememberDecodedObject(
        jsonString = lineJson,
        default = UiConstants.defaultLineCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding lineObject" }
    }

    val angleLineJson by AngleLineSettingsStore.angleLineJson.asState()
    val angleLineObject = rememberDecodedObject(
        jsonString = angleLineJson,
        default = UiConstants.defaultAngleCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding angleLineObject" }
    }

    val startLineJson by AngleLineSettingsStore.startLineJson.asState()
    val startLineObject = rememberDecodedObject(
        jsonString = startLineJson,
        default = UiConstants.defaultStartCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding startLineObject" }
    }

    val endLineJson by AngleLineSettingsStore.endLineJson.asState()
    val endLineObject = rememberDecodedObject(
        jsonString = endLineJson,
        default = UiConstants.defaultEndCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding endLineObject" }
    }

    val holdCustomObjectJson by HoldToActivateArcSettingsStore.holdToActivateArcCustomObject.asState()
    val holdCustomObject = rememberDecodedObject(
        jsonString = holdCustomObjectJson,
        default = UiConstants.defaultHoldCustomObject,
        json = json
    ) {
        logW(ANGLE_LINE_TAG) { "Error decoding endLineObject" }
    }

    val layersOrder by rememberMainScreenLayerOrder()

    val showTooltipsOnAddPointDialog by UiSettingsStore.showTooltipsOnAddPointDialog.asState()


    /**
     * Main Composition local provider, I just for everything I can here to avoid having to import them everywhere
     * I know that I should carefully review what global locals I add, but until now it worked to I'll keep it that way until I notice lag
     */
    CompositionLocalProvider(
        LocalDefaultPoint provides defaultPoint,

        LocalDrawerIconsCache provides drawerIconCache,
        LocalPointIconsCache provides pointsIconCache,

        LocalIconShape provides iconsShape,
        LocalPoints provides points,
        LocalNests provides nests,
        LocalStatusBarElements provides elements,

        LocalLineObject provides lineObject,

        LocalAngleLineObject provides angleLineObject,
        LocalStartLineObject provides startLineObject,
        LocalEndLineObject provides endLineObject,
        LocalHoldCustomObject provides holdCustomObject,

        LocalMainScreenLayers provides layersOrder,
        LocalShowLabelsInAddPointDialog provides showTooltipsOnAddPointDialog,

        LocalDisableHapticFeedbackGlobally provides disableHapticFeedbackGlobally,

        LocalAppItemSettings provides rememberAppItemSettings()
    ) {
        OverlayHost(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Scaffold(
                floatingActionButton = {
                    if (colorTestMode) {
                        FloatingActionButton(
                            onClick = { navController.navigate(SETTINGS.COLORS) },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.back_to_colors_settings)
                            )
                        }
                    }
                },
                snackbarHost = {
                    LauncherSnackbarHost()
                },
                contentWindowInsets = WindowInsets(),
                containerColor = containerColor,
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = ROUTES.MAIN,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    // Main App (LauncherScreen)
                    composable(
                        route = ROUTES.MAIN,
                        enterTransition = { if (drawerEnterExitAnimations) raiseUpAnimation() else EnterTransition.None },
                        exitTransition = { if (drawerEnterExitAnimations) collapseDownAnimation() else ExitTransition.None },
                        popEnterTransition = { if (drawerEnterExitAnimations) raiseUpAnimation() else EnterTransition.None },
                        popExitTransition = { if (drawerEnterExitAnimations) collapseDownAnimation() else ExitTransition.None },
                    ) {
                        MainScreen(::launchAction)
                    }

                    composable(
                        route = ROUTES.DRAWER,
                        enterTransition = { if (drawerEnterExitAnimations) raiseUpAnimation() else EnterTransition.None },
                        exitTransition = { if (drawerEnterExitAnimations) collapseDownAnimation() else ExitTransition.None },
                        popEnterTransition = { if (drawerEnterExitAnimations) raiseUpAnimation() else EnterTransition.None },
                        popExitTransition = { if (drawerEnterExitAnimations) collapseDownAnimation() else ExitTransition.None },
                    ) {
                        AppDrawerScreen(
                            autoShowKeyboard = autoShowKeyboardOnDrawer,
                            onRegisterHomeHandler = { handler ->
                                drawerHomeHandler = handler
                            },
                            drawerToolbarsOrder = selectedToolbarItems,
                            leftAction = leftDrawerAction,
                            leftWeight = leftDrawerWidth,
                            rightAction = rightDrawerAction,
                            rightWeight = rightDrawerWidth,
                            onLaunchAction = {
                                popBackMainScreen()
                                launchAction(it)
                            },
                            onClose = ::popBackMainScreen
                        )
                    }

                    // Welcome screen
                    settingComposable(ROUTES.WELCOME) {
                        WelcomeScreen(
                            onEnterSettings = ::popBackToSettingsRoot,
                            onEnterApp = ::popBackMainScreen
                        )
                    }


                    /* ───────────── Settings navigation ───────────── */
                    navigation(
                        startDestination = startDestination,
                        route = "settings_graph"
                    ) {
                        settingComposable(SETTINGS.ROOT) {
                            SettingsScreen(
                                onAdvSettings = {
                                    goSettings(SETTINGS.ADVANCED_ROOT, false)
                                },
                                onNestEdit = {
                                    goSettings(EDIT_SCREENS.NESTS_EDIT.replace("{id}", it.toString()), false)
                                },
                                onBack = ::popBackMainScreen
                            )
                        }

                        settingComposable(SETTINGS.ADVANCED_ROOT) { AdvancedSettingsScreen { popBackToSettingsRoot() } }

                        // All the nested settings screens
                        settingComposable(SETTINGS.APPEARANCE) { AppearanceTab(::popBackToAdvSettingsRoot) }
//                        settingComposable(SETTINGS.PERMISSIONS) { PermissionsTab { popBackToAdvSettingsRoot() } }
                        settingComposable(SETTINGS.BEHAVIOR) { BehaviorTab(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.DRAWER) { DrawerTab(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.LANGUAGE) { LanguageTab(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.BACKUP) { BackupTab(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.CHANGELOGS) { ChangelogsScreen(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.EXTENSIONS) { ExtensionsTab(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.WELLBEING) { WellbeingTab(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.DEBUG) { DebugTab(::popBackToAdvSettingsRoot) }
                        settingComposable(SETTINGS.LOGS) { LogsTab(::popBackToDebug) }
                        settingComposable(SETTINGS.SETTINGS_JSON) { SettingsDebugTab(::popBackToDebug) }

                        // All the appearance sub-settings
                        settingComposable(SETTINGS.COLORS) { ColorSelectorTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.THEME) { ThemesTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.WALLPAPER) { WallpaperTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.ICON_PACK) { IconPackTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.STATUS_BAR) { StatusBarTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.FONTS) { FontTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.ANGLE_LINE_EDIT) { AngleLineTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.HOLD_TO_ACTIVATE_ARC) { HoldToActivateArcTab(::popBackToAppearance) }
                        settingComposable(SETTINGS.MAINS_SCREEN_LAYERS) { MainScreeLayersTab(::popBackToAppearance) }

                        settingComposable(
                            route = SETTINGS.LOGS_VIEWER_SCREEN,
                            arguments = listOf(navArgument("filename") { type = NavType.StringType }),
                        ) { backStack ->
                            LogsViewerScreen(
                                filename = backStack.arguments!!.getString("filename")!!,
                                onBack = {
                                    goSettings(SETTINGS.LOGS)
                                }
                            )
                        }

                        settingComposable(
                            route = EDIT_SCREENS.NESTS_EDIT,
                            arguments = listOf(navArgument("id") { type = NavType.StringType }),
                        ) { backStack ->
                            NestEditingScreen(
                                nestId = backStack.arguments!!.getString("id")!!.toInt(),
                                onBack = ::popBackToSettingsRoot
                            )
                        }

                        settingComposable(
                            route = SETTINGS.WIDGETS_FLOATING_APPS,
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStack ->
                            FloatingAppsTab(
                                onBack = ::popBackToAppearance,
                                onLaunchSystemWidgetPicker = ::launchWidgetsPicker,
                                onResetWidgetSize = onResetWidgetSize,
                                onRemoveWidget = onRemoveFloatingApp,
                                initialNestId = backStack.arguments!!.getString("id").takeIf { it != "{id}" }?.toInt() ?: 0
                            )
                        }

                        settingComposable(SETTINGS.WORKSPACE) {
                            WorkspaceListScreen(
                                onOpenWorkspace = { id ->
                                    navController.navigate(
                                        EDIT_SCREENS.WORKSPACE_DETAIL.replace("{id}", id)
                                    )
                                },
                                onBack = ::popBackToAdvSettingsRoot
                            )
                        }

                        settingComposable(
                            route = EDIT_SCREENS.WORKSPACE_DETAIL,
                            arguments = listOf(navArgument("id") { type = NavType.StringType }),
                        ) { backStack ->
                            WorkspaceDetailScreen(
                                workspaceId = backStack.arguments!!.getString("id")!!,
                                onBack = { navController.popBackStack() },
                                onLaunchAction = ::launchAction
                            )
                        }
                    }
                }
            }
        }

        if (showShizukuCommandPromter != null) {
            AdbCommandInputDialog(
                onDismiss = { showShizukuCommandPromter = null },
                showLeaveEmptyNotice = false
            ) {
                if (it.command.trim().isNotEmpty()){
                    runShisukuCommandNotEmpty(it)
                }
            }
        }

        if (shizukuOutput != null) {
            ShizukuOutputDialog(shizukuOutput!!) {
                shizukuViewModel.clearOutput()
            }
        }

        if (showSetAsDefaultBanner || showReselectAutoBackupFile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Spacer()
                AnimatedVisibility(showSetAsDefaultBanner) {
                    SetDefaultLauncherBanner()
                }
                AnimatedVisibility(showReselectAutoBackupFile) {
                    ReselectAutoBackupBanner()
                }
            }
        }

        if (showFps) {
            FpsCounterGraph()
        }

        if (showFilePicker != null) {
            val currentPoint = showFilePicker!!

            FilePickerDialog(
                onDismiss = { showFilePicker = null },
                onFileSelected = { newAction ->

                    // Build the updated point
                    val updatedPoint = currentPoint.copy(action = newAction)

                    // Replace only this point
                    val finalList = points.map { p ->
                        if (p.id == currentPoint.id) updatedPoint else p
                    }


                    scope.launch {
                        SwipeSettingsStore.savePoints(ctx, finalList)
                    }

                    showFilePicker = null
                }
            )
        }

        if (showWhatsNewBottomSheet) {
            WhatsNewBottomSheet(
                updates = updates
            ) {
                showWhatsNewBottomSheet = false
                scope.launch {
                    PrivateSettingsStore.lastSeenVersionCodeWhatsNew.set(
                        ctx,
                        currentVersionCode
                    )
                }
            }
        }

        if (showWidgetPicker != null) {
            val nestToBind = showWidgetPicker!!
            WidgetPickerDialog(
                onBindCustomWidget = { id, info ->
                    onBindCustomWidget(id, info, nestToBind)
                }
            ) { showWidgetPicker = null }
        }

        if (showShizukuUnavailableDialog) {
            ShizukuUnavailableDialog(
                onDismiss = { showShizukuUnavailableDialog = false },
                onConfirm = {
                    if (isShizukuInstalled) launchAction(SwipeActionSerializable.LaunchApp(SHIZUKU_PACKAGE_NAME, false, 0))
                    else ctx.openUrl(
                        url = URL_SHIZUKU_SITE
                    )
                }
            )
        }

        /* ───────────── RESULT DIALOG ( IMPORT / EXPORT ) ───────────── */
        result?.let { res ->
            val isError = res.error
            val isExport = res.export
            val errorMessage = res.message

            // Reload the whole viewModel data after restore
            LaunchedEffect(res) {
                scope.launch(Dispatchers.IO) {
                    appsViewModel.loadAll()
                }
            }

            UserValidation(
                title = when {
                    isError && isExport -> stringResource(R.string.export_failed)
                    isError && !isExport -> stringResource(R.string.import_failed)
                    !isError && isExport -> stringResource(R.string.export_successful)
                    else -> stringResource(R.string.import_successful)
                },
                message = when {
                    isError -> errorMessage.ifBlank { stringResource(R.string.unknown_error) }
                    isExport -> stringResource(R.string.export_successful)
                    else -> null
                },
                titleIcon = if (isError) Icons.Default.Warning else Icons.Default.Check,
                titleColor = if (isError) MaterialTheme.colorScheme.error else Color.Green,
                copy = isError,
                onValidate = { backupViewModel.setResult(null) }
            )
        }

        /* ────────── PIN unlock dialog ────────── */
        if (showPinDialog != null) {
            val routeQuery = showPinDialog!!
            var pin by remember { mutableStateOf("") }
            val pinShapes = remember { mutableStateListOf<IconShape>() }
            var failedTries by remember { mutableIntStateOf(0) }

            PinUnlockDialog(
                onDismiss = { showPinDialog = null; pinError = null },
                onValidate = {
                    if (SecurityHelper.verifyPin(pin, pinHash)) {
                        isUnlocked = true
                        showPinDialog = null
                        pinError = null
                        goSettings(routeQuery, false)
                    } else {
                        pinError = ctx.getString(R.string.wrong_pin)
                        failedTries++
                    }
                    pinShapes.clear()
                    pin = ""
                },
                errorMessage = pinError,
                pin = { pin },
                pinShapes = { pinShapes },
                failedTries = { failedTries },
                onPinChanged = { newValue ->
                    pinError = null
                    pin = newValue
                    if (pinShapes.size < newValue.length) {
                        repeat(newValue.length - pinShapes.size) {
                            pinShapes.add(allShapesWithoutRandom.random())
                        }
                    } else {
                        repeat(pinShapes.size - newValue.length) {
                            pinShapes.removeAt(pinShapes.lastIndex)
                        }
                    }
                }
            )
        }


        if (lastSeenVersionCodeGoogleLockdownWarning < currentVersionCode) {
            GoogleLockingWarning(
                onSolution = {
                    ctx.openUrl("https://keepandroidopen.org/")
                    scope.launch {
                        PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.set(
                            ctx,
                            currentVersionCode
                        )
                    }
                }
            ) {
                scope.launch {
                    PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.set(
                        ctx,
                        currentVersionCode
                    )
                }
            }
        }

        // Private space optional debug info
        val state by privateSpaceState.collectAsState()
        val privateSpaceDebugInfo by DebugSettingsStore.privateSpaceDebugInfo.asState()
        AnimatedVisibility(privateSpaceDebugInfo) {
            PrivateSpaceStateDebugScreen(state)
        }
    }
}
