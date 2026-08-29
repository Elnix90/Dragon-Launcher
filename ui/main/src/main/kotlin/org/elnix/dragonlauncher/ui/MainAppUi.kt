package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.SHIZUKU_TAG
import org.elnix.dragonlauncher.TAG
import org.elnix.dragonlauncher.base.Constants.PackageNames.SHIZUKU_PACKAGE_NAME
import org.elnix.dragonlauncher.base.Constants.URLs.URL_SHIZUKU_SITE
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Device
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.None
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Pattern
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Pin
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.navigation.halfTransparentScreen
import org.elnix.dragonlauncher.base.navigation.inTransparentScreen
import org.elnix.dragonlauncher.base.navigation.isIgnoredReturnScreen
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ktx.findFragmentActivity
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.AppLaunchViewModel
import org.elnix.dragonlauncher.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.models.ShizukuViewModel
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.timer.AppTimerService.Companion.EXTRA_APP_NAME
import org.elnix.dragonlauncher.timer.AppTimerService.Companion.SHOW_LAUNCHER
import org.elnix.dragonlauncher.ui.actions.launchAction
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asMutableState
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.compositionslocals.Navigator
import org.elnix.dragonlauncher.ui.compositionslocals.ProvideGlobalCompositionLocals
import org.elnix.dragonlauncher.ui.dialogs.AdbCommandInputDialog
import org.elnix.dragonlauncher.ui.dialogs.BackupResultDialog
import org.elnix.dragonlauncher.ui.dialogs.FilePickerDialog
import org.elnix.dragonlauncher.ui.dialogs.GoogleLockingWarningDialog
import org.elnix.dragonlauncher.ui.dialogs.MainScreeLayersTab
import org.elnix.dragonlauncher.ui.dialogs.ShizukuOutputDialog
import org.elnix.dragonlauncher.ui.dialogs.ShizukuUnavailableDialog
import org.elnix.dragonlauncher.ui.dialogs.security.PatternSetup
import org.elnix.dragonlauncher.ui.dialogs.security.PatternUnlock
import org.elnix.dragonlauncher.ui.dialogs.security.PinSetup
import org.elnix.dragonlauncher.ui.dialogs.security.PinUnlock
import org.elnix.dragonlauncher.ui.drawer.AppDrawerScreen
import org.elnix.dragonlauncher.ui.helpers.BottomBanners
import org.elnix.dragonlauncher.ui.helpers.FpsCounterGraph
import org.elnix.dragonlauncher.ui.helpers.LauncherSnackbarHost
import org.elnix.dragonlauncher.ui.navigation.drawerMetadata
import org.elnix.dragonlauncher.ui.navigation.horizontalMetadata
import org.elnix.dragonlauncher.ui.navigation.verticalMetadata
import org.elnix.dragonlauncher.ui.settings.backup.BackupTab
import org.elnix.dragonlauncher.ui.settings.customization.AngleLineTab
import org.elnix.dragonlauncher.ui.settings.customization.AppDisplayTab
import org.elnix.dragonlauncher.ui.settings.customization.AppearanceTab
import org.elnix.dragonlauncher.ui.settings.customization.BehaviorTab
import org.elnix.dragonlauncher.ui.settings.customization.ColorSelectorTab
import org.elnix.dragonlauncher.ui.settings.customization.FontTab
import org.elnix.dragonlauncher.ui.settings.customization.HoldToActivateTab
import org.elnix.dragonlauncher.ui.settings.customization.IconsTab
import org.elnix.dragonlauncher.ui.settings.customization.NestEditScreen
import org.elnix.dragonlauncher.ui.settings.customization.StatusBarTab
import org.elnix.dragonlauncher.ui.settings.customization.ThemesTab
import org.elnix.dragonlauncher.ui.settings.customization.WallpaperTab
import org.elnix.dragonlauncher.ui.settings.customization.WidgetsTab
import org.elnix.dragonlauncher.ui.settings.customization.drawer.DrawerTab
import org.elnix.dragonlauncher.ui.settings.debug.DebugTab
import org.elnix.dragonlauncher.ui.settings.debug.LogsTab
import org.elnix.dragonlauncher.ui.settings.debug.LogsViewerScreen
import org.elnix.dragonlauncher.ui.settings.debug.SettingsDebugTab
import org.elnix.dragonlauncher.ui.settings.extensions.ExtensionsTab
import org.elnix.dragonlauncher.ui.settings.wellbeing.WellbeingTab
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceDetailScreen
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspacesTab
import org.elnix.dragonlauncher.ui.warning.SignatureWarningDialog
import org.elnix.dragonlauncher.ui.welcome.WelcomeScreen
import org.elnix.dragonlauncher.ui.wellbeing.DigitalPauseScreen
import org.elnix.dragonlauncher.ui.wellbeing.TimeLimitExceededScreen
import org.elnix.dragonlauncher.ui.whatsnew.ChangelogsScreen
import org.elnix.dragonlauncher.ui.whatsnew.WhatsNewBottomSheet
import rikka.shizuku.Shizuku


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MainAppUi(
    appLifecycleViewModel: AppLifecycleViewModel = activityViewModel(),
    drawerViewModel: DrawerViewModel = activityViewModel(),
    securityViewModel: SecurityViewModel = activityViewModel(),
    appLaunchViewModel: AppLaunchViewModel = activityViewModel(),
    shizukuViewModel: ShizukuViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    swipeViewModel: SwipeViewModel = activityViewModel(),
    onBindCustomWidget: (Int, ComponentName, nestId: Int) -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit,
    onRemoveWidget: (Widget) -> Unit
) {
    val ctx = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scope = rememberCoroutineScope()
    val pointsService = pointsViewModel.pointsService

    var showFilePicker: Point? by remember { mutableStateOf(null) }
    var showShizukuCommandPromter by remember { mutableStateOf<Action.RunAdbCommand?>(null) }

    val showShizukuUnavailableDialog by shizukuViewModel.showUnavailable.collectAsState()
    val hasShizukuPermission by shizukuViewModel.shizukuPermissionState().collectAsState()
    val isShizukuInstalled by drawerViewModel.isAppInstalled(SHIZUKU_PACKAGE_NAME).collectAsState()


    val homeAction by BehaviorSettingsStore.homeAction.asState()
    val doubleClickAction by BehaviorSettingsStore.doubleClickAction.asStateNull()

    val useAccessibilityInsteadOfContextToExpandActionPanel by DebugSettingsStore.useAccessibilityInsteadOfContextToExpandActionPanel.asState()


    val backStack = rememberNavBackStack(NavigationRoute.Main)

    // Forced to use a state here, to make compose react to the changes, especially the hole events handles
    val currentRoute by remember {
        derivedStateOf { backStack.lastOrNull() ?: NavigationRoute.Main }
    }

    val isLocked by securityViewModel.isLocked.asState()
    val lockMethod by PrivateSettingsStore.lockMethod.asState()

    LaunchedEffect(currentRoute) {
        securityViewModel.onEnterNewRoute(currentRoute)
    }

    val navigator: Navigator = object : Navigator {

        override fun go(route: NavigationRoute) {
            backStack.remove(route)
            backStack.add(route)
        }

        override fun navigate(route: NavigationRoute) {
            if (!isLocked) {
                go(route)
                return
            }

            if (backStack.any { it is NavigationRoute.LockScreen }) return

            if (route in NavigationRoute.settingsRoutes && lockMethod != None) {
                backStack.add(NavigationRoute.LockScreen(route))
            } else {
                go(route)
            }
        }

        override fun onBack() {
            // Popping the only screen will crash so this avoids it
            if (backStack.size == 1) return
            backStack.removeLastOrNull()
        }

        override fun popBackMainScreen() {
            backStack.clear()
            backStack.add(NavigationRoute.Main)
        }
    }


    val lastInteraction by appLifecycleViewModel.lastInteraction.asState()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val offScreenUserTimeout = lastInteraction.takeIf { it != -1L }

                if (offScreenUserTimeout != null) {
                    val isInIgnoredRoutes = currentRoute.isIgnoredReturnScreen

                    val userHasExceededTimeout = appLifecycleViewModel.isTimeoutExceeded(offScreenUserTimeout)

                    if (!isInIgnoredRoutes && userHasExceededTimeout) {
                        navigator.popBackMainScreen()
                    }
                }

            } else if (event == Lifecycle.Event.ON_PAUSE) {
                appLifecycleViewModel.updateLastInteraction()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the noAnimComposable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val launcherReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SHOW_LAUNCHER) {
                val appName = intent.getStringExtra(EXTRA_APP_NAME)
                navigator.navigate(NavigationRoute.TimerExceeded(appName ?: "Unknown App"))
            }
        }
    }
    LaunchedEffect(Unit) {
        registerReceiver(
            ctx,
            launcherReceiver,

            IntentFilter("com.elnix.dragonlauncher.SHOW_LAUNCHER"), ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    fun runShisukuCommandNotEmpty(command: Action.RunAdbCommand) {
        if (!Shizuku.pingBinder()) {
            logD(SHIZUKU_TAG) { "Shizuku is not running, opening it..." }
            shizukuViewModel.setUnavailable()
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

    fun launchAction(point: Point) {
        val action = point.action

        swipeViewModel.clearAfterLaunch()
        appLifecycleViewModel.blockHomeActionsTemporarily()

        try {
            launchAction(
                ctx = ctx,
                appLaunchViewModel = appLaunchViewModel,
                drawerViewModel = drawerViewModel,
                action = action,
                useAccessibilityInsteadOfContextToExpandActionPanel = useAccessibilityInsteadOfContextToExpandActionPanel,
                onReselectFile = { showFilePicker = point },
                onAppSettings = navigator::navigate,
                onAppDrawer = { workspaceId ->
                    if (workspaceId != null) {
                        scope.launch {
                            DrawerSettingsStore.lastWorkspaceUsed.set(ctx, workspaceId)
                        }
                    }
                    navigator.navigate(NavigationRoute.Drawer)
                }
            ) { command ->
                if (command.command.trim().isEmpty()) {
                    showShizukuCommandPromter = command
                } else {
                    runShisukuCommandNotEmpty(command)
                }
            }
        } catch (e: Exception) {
            logE(TAG, e) { "Unknow error while launching action" }
        }
    }

    fun launchAction(action: Action) {
        launchAction(dummySwipePoint(action))
    }

    // Drawer home action receiver
    var drawerHomeHandler by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        appLifecycleViewModel.homeEvents.collect {

            logD(TAG) { "Got home event, launching home action, currentRoute: $currentRoute" }
            when (currentRoute) {
                NavigationRoute.Drawer -> {
                    drawerHomeHandler?.invoke()
                }

                NavigationRoute.Main -> {
                    launchAction(homeAction)
                }

                NavigationRoute.Welcome -> {
                    // Do nothing when in welcome screen
                }

                // Return to home screen in case of any home action in the settings
                else -> {
                    navigator.popBackMainScreen()
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        swipeViewModel.doubleClicActionChannel.collect {
            if (doubleClickAction != null) {
                launchAction(doubleClickAction!!)
            }
        }
    }

    val pointsScreensTransparency by UiSettingsStore.pointsScreensTransparency.asState()

    val containerColor by animateColorAsState(
        targetValue = when {
            currentRoute.inTransparentScreen -> Color.Transparent
            currentRoute.halfTransparentScreen -> MaterialTheme.colorScheme.background.alphaMultiplier(pointsScreensTransparency)
            else -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(500)
    )

    val colorTestMode by ColorModesSettingsStore.colorTestMode.asState()


    val hasSeenWelcome by PrivateSettingsStore.hasSeenWelcome.asStateNull()
    LaunchedEffect(hasSeenWelcome) {
        if (hasSeenWelcome == false) {
            navigator.navigate(NavigationRoute.Welcome)
        }
    }

    ProvideGlobalCompositionLocals {
        CompositionLocalProvider(
            LocalNavigator provides navigator
        ) {
            Scaffold(
                floatingActionButton = {
                    if (colorTestMode) {
                        AnimatedFab(
                            onClick = { navigator.navigate(NavigationRoute.Colors) },
                            icon = R.drawable.edit_rounded,
                            modifier = Modifier.padding(bottom = 80.dp)
                        )
                    }
                },
                topBar = { FpsCounterGraph() },
                snackbarHost = { LauncherSnackbarHost() },
                contentWindowInsets = WindowInsets(),
                containerColor = containerColor
            ) { paddingValues ->

                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.padding(paddingValues),
                    onBack = { navigator.onBack() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    predictivePopTransitionSpec = {
                        ContentTransform(
                            fadeIn(),
                            slideOutHorizontally { it },
                        )
                    },
                    popTransitionSpec = {
                        ContentTransform(
                            fadeIn(),
                            slideOutHorizontally { it },
                        )
                    },
                    entryProvider = entryProvider {

                        entry<NavigationRoute.Main>(metadata = verticalMetadata) { MainScreen(::launchAction) }
                        entry<NavigationRoute.Drawer>(metadata = drawerMetadata) {
                            LaunchedEffect(Unit) {
                                drawerViewModel.clearSearchQuery()
                            }
                            AppDrawerScreen(
                                onRegisterHomeHandler = { handler ->
                                    // This part of the code is frightful
                                    drawerHomeHandler = handler
                                },
                                onLaunchAction = {
                                    drawerViewModel.clearSearchQuery()
                                    launchAction(it)
                                    navigator.onBack()
                                }
                            )
                        }

                        entry<NavigationRoute.Welcome>(metadata = horizontalMetadata) { WelcomeScreen() }
                        entry<NavigationRoute.PointsSettings>(metadata = horizontalMetadata) { PointsSettingsScreen() }
                        entry<NavigationRoute.Settings>(metadata = horizontalMetadata) { SettingsScreen() }
                        entry<NavigationRoute.Appearance>(metadata = horizontalMetadata) { AppearanceTab() }
                        entry<NavigationRoute.Behavior>(metadata = horizontalMetadata) { BehaviorTab() }
                        entry<NavigationRoute.DrawerSettings>(metadata = horizontalMetadata) { DrawerTab() }
                        entry<NavigationRoute.Backup>(metadata = horizontalMetadata) { BackupTab() }
                        entry<NavigationRoute.Changelogs>(metadata = horizontalMetadata) { ChangelogsScreen() }
                        entry<NavigationRoute.Extensions>(metadata = horizontalMetadata) { ExtensionsTab() }
                        entry<NavigationRoute.Wellbeing>(metadata = horizontalMetadata) { WellbeingTab() }
                        entry<NavigationRoute.Debug>(metadata = horizontalMetadata) { DebugTab() }
                        entry<NavigationRoute.Logs>(metadata = horizontalMetadata) { LogsTab() }
                        entry<NavigationRoute.SettingsJson>(metadata = horizontalMetadata) { SettingsDebugTab() }

                        // All the appearance sub-settings
                        entry<NavigationRoute.AppDisplay>(metadata = horizontalMetadata) { AppDisplayTab() }
                        entry<NavigationRoute.Colors>(metadata = horizontalMetadata) { ColorSelectorTab() }
                        entry<NavigationRoute.Theme>(metadata = horizontalMetadata) { ThemesTab() }
                        entry<NavigationRoute.Wallpaper>(metadata = horizontalMetadata) { WallpaperTab() }
                        entry<NavigationRoute.Icons>(metadata = horizontalMetadata) { IconsTab() }
                        entry<NavigationRoute.StatusBar>(metadata = horizontalMetadata) { StatusBarTab() }
                        entry<NavigationRoute.Fonts>(metadata = horizontalMetadata) { FontTab() }
                        entry<NavigationRoute.AngleLineEdit>(metadata = horizontalMetadata) { AngleLineTab() }
                        entry<NavigationRoute.HoldToActivateArc>(metadata = horizontalMetadata) { HoldToActivateTab() }
                        entry<NavigationRoute.MainScreenLayers>(metadata = horizontalMetadata) { MainScreeLayersTab() }
                        entry<NavigationRoute.Workspace>(metadata = horizontalMetadata) { WorkspacesTab() }

                        entry<NavigationRoute.NestEdit>(metadata = horizontalMetadata) { NestEditScreen() }

                        entry<NavigationRoute.LogsViewer>(metadata = horizontalMetadata) { key -> LogsViewerScreen(key.filename) }

                        entry<NavigationRoute.Widgets>(metadata = horizontalMetadata) { key ->
                            WidgetsTab(
                                onBindCustomWidget = onBindCustomWidget,
                                onResetWidgetSize = onResetWidgetSize,
                                onRemoveWidget = onRemoveWidget
                            )
                        }

                        entry<NavigationRoute.WorkspaceDetail>(metadata = horizontalMetadata) { key -> WorkspaceDetailScreen(key.workspaceId) }

                        entry<NavigationRoute.TimerExceeded> { key -> TimeLimitExceededScreen(key.appName) }
                        entry<NavigationRoute.LockScreen> { key ->

                            fun onSuccess() {
                                backStack.remove(key)
                                navigator.go(key.screenToGo)
                                securityViewModel.unlock()
                            }
                            fun onDismiss() { backStack.remove(key) }

                            when (lockMethod) {
                                None -> {
                                    // This block is only called when the user pressed the secret unlock button
                                    onSuccess()
                                }

                                Pin -> {
                                    PinUnlock(
                                        onDismiss = ::onDismiss,
                                        onSuccess = ::onSuccess
                                    )
                                }

                                Pattern -> {
                                    PatternUnlock(
                                        onDismiss = ::onDismiss,
                                        onSuccess = ::onSuccess
                                    )
                                }

                                Device -> {
                                    LaunchedEffect(Unit) {
                                        val activity = ctx.findFragmentActivity()
                                        if (activity != null && securityViewModel.isDeviceUnlockAvailable()) {
                                            securityViewModel.showDeviceUnlockPrompt(
                                                activity = activity,
                                                onSuccess = ::onSuccess,
                                                onError = { msg ->
                                                    ctx.showToast(ctx.getString(R.string.authentication_error, msg))
                                                    onDismiss()
                                                },
                                                onFailed = {
                                                    ctx.showToast(ctx.getString(R.string.authentication_failed))
                                                    onDismiss()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        entry<NavigationRoute.LockScreenSetup>(metadata = verticalMetadata) { key ->
                            val lockMethod = key.lockMethod

                            fun onDismiss() {
                                backStack.remove(key)
                            }

                            when (lockMethod) {
                                None -> {
                                    securityViewModel.removeLock()
                                    onDismiss()
                                }

                                Pin -> {
                                    PinSetup(
                                        onDismiss = ::onDismiss,
                                        onPinSet = { pin ->
                                            securityViewModel.setPinLockMethod(pin)
                                            onDismiss()
                                        }
                                    )
                                }

                                Pattern -> {
                                    PatternSetup(
                                        onDismiss = ::onDismiss,
                                        onPattern = { pattern ->
                                            securityViewModel.setPatternLockMethod(pattern)
                                            onDismiss()
                                        }
                                    )
                                }

                                Device -> {
                                    LaunchedEffect(Unit) {
                                        val activity = ctx.findFragmentActivity()
                                        if (activity != null && securityViewModel.isDeviceUnlockAvailable()) {
                                            securityViewModel.showDeviceUnlockPrompt(
                                                activity = activity,
                                                onSuccess = {
                                                    securityViewModel.setDeviceLockScreenMethod()
                                                    onDismiss()
                                                },
                                                onError = { msg ->
                                                    ctx.showToast(ctx.getString(R.string.authentication_error, msg))
                                                    onDismiss()
                                                },
                                                onFailed = {
                                                    ctx.showToast(ctx.getString(R.string.authentication_failed))
                                                    onDismiss()
                                                }
                                            )
                                        } else {
                                            ctx.showToast(ctx.getString(R.string.device_credentials_not_available))
                                            securityViewModel.removeLock()
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                if (currentRoute !is NavigationRoute.LockScreen && currentRoute !is NavigationRoute.LockScreenSetup) {
                    if (showFilePicker != null) {
                        val currentPoint = showFilePicker!!

                        FilePickerDialog(
                            onDismiss = { showFilePicker = null },
                            onFileSelected = { newAction ->
                                val updatedPoint = currentPoint.copy(action = newAction)
                                pointsService.editPoint(currentPoint.id) { updatedPoint }
                                showFilePicker = null
                                launchAction(updatedPoint)
                            }
                        )
                    }


                    if (showShizukuCommandPromter != null) {
                        AdbCommandInputDialog(
                            onDismiss = { showShizukuCommandPromter = null },
                            showLeaveEmptyNotice = false
                        ) {
                            if (it.command.trim().isNotEmpty()) {
                                runShisukuCommandNotEmpty(it)
                            }
                        }
                    }

                    if (showShizukuUnavailableDialog) {
                        ShizukuUnavailableDialog(
                            onDismiss = {
                                shizukuViewModel.dismissUnavailableDialog()
                            },
                            onConfirm = {
                                if (isShizukuInstalled) launchAction(Action.LaunchApp(SHIZUKU_PACKAGE_NAME, Profile.dummy()))
                                else uriHandler.openUri(URL_SHIZUKU_SITE)
                            }
                        )
                    }

                    var pendingAppToLaunch by appLaunchViewModel.pendingAppLaunch.asMutableState()

                    if (pendingAppToLaunch != null) {
                        val pendingApp = pendingAppToLaunch!!
                        DigitalPauseScreen(
                            application = pendingApp,
                            onCancel = { pendingAppToLaunch = null }
                        )
                    }

                    BottomBanners(currentRoute)
                    ShizukuOutputDialog()
                    WhatsNewBottomSheet()
                    BackupResultDialog()
                    GoogleLockingWarningDialog()
                    SignatureWarningDialog()
                }
            }
        }
    }
}