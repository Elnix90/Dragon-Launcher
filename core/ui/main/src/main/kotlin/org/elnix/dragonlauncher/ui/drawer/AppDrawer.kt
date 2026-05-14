package org.elnix.dragonlauncher.ui.drawer

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.base.ktx.px
import org.elnix.dragonlauncher.base.ktx.toDp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants
import org.elnix.dragonlauncher.common.messyfolder.openSearch
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.WorkspaceType
import org.elnix.dragonlauncher.common.utils.PrivateSpaceUtils
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.CLEAR
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.CLOSE
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.CLOSE_KB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.DISABLED
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.NONE
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.OPEN_FIRST_APP
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.OPEN_KB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.SEARCH_WEB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.TOGGLE_KB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.RecentlyUsed
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.SearchBar
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.Spacer
import org.elnix.dragonlauncher.enumsui.toggle.isUsed
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalPrivateSpaceViewModel
import org.elnix.dragonlauncher.ui.dialogs.AppAliasesDialog
import org.elnix.dragonlauncher.ui.dialogs.AppIconEditor
import org.elnix.dragonlauncher.ui.dialogs.AppLongPressRow
import org.elnix.dragonlauncher.ui.dialogs.TextEditorDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.helpers.AppDrawerSearch
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.ui.helpers.WallpaperDim
import kotlin.math.abs
import kotlin.math.pow

@SuppressLint("LocalContextGetResourceValueCall")
@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDrawerScreen(
    autoShowKeyboard: Boolean,
    drawerToolbarsOrder: List<DrawerToolbar>,
    leftAction: DrawerActions,
    leftWeight: Float,
    rightAction: DrawerActions,
    rightWeight: Float,
    onRegisterHomeHandler: ((() -> Unit)?) -> Unit,
    onNavigate: (NavigationRoute) -> Unit,
    onLaunchAction: (SwipeActionSerializable) -> Unit,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val appsViewModel = LocalAppsViewModel.current
    val privateSpaceViewModel = LocalPrivateSpaceViewModel.current

    val privateSpaceState by appsViewModel.privateSpaceState.collectAsState()

    val workspaceState by appsViewModel.enabledState.collectAsState()
    val visibleWorkspaces = workspaceState.workspaces
    val overrides = workspaceState.appOverrides
    val aliases = workspaceState.appAliases


    val selectedWorkspaceId by appsViewModel.selectedWorkspaceId.collectAsState()
    val initialIndex = visibleWorkspaces.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (visibleWorkspaces.size - 1).coerceAtLeast(0)),
        pageCount = { visibleWorkspaces.size }
    )

    val autoLaunchSingleMatch by DrawerSettingsStore.autoOpenSingleMatch.asState()
    val disableAutoLaunchOnSpaceFirstChar by DrawerSettingsStore.disableAutoLaunchOnSpaceFirstChar.asState()

    /* ───────────── Actions ───────────── */
    val tapEmptySpaceToRaiseKeyboard by DrawerSettingsStore.tapEmptySpaceAction.asState()
    val drawerEnterAction by DrawerSettingsStore.drawerEnterAction.asState()
    val drawerBackAction by DrawerSettingsStore.backDrawerAction.asState()
    val drawerHomeAction by DrawerSettingsStore.drawerHomeAction.asState()
    val drawerScrollDownAction by DrawerSettingsStore.scrollDownDrawerAction.asState()
    val drawerScrollUpAction by DrawerSettingsStore.scrollUpDrawerAction.asState()


    val showSearchBar by DrawerSettingsStore.showSearchBar.asState()


    /* ───────────── Recently Used Apps ───────────── */
    val showRecentlyUsedApps by DrawerSettingsStore.showRecentlyUsedApps.asState()
    val recentlyUsedAppsCount by DrawerSettingsStore.recentlyUsedAppsCount.asState()
    val recentApps by appsViewModel.getRecentApps(recentlyUsedAppsCount)
        .collectAsStateWithLifecycle(emptyList())


    var haveToLaunchFirstApp by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }

    var renameAppTarget by remember { mutableStateOf<AppModel?>(null) }
    var showAliasDialog by remember { mutableStateOf<AppModel?>(null) }

    var workspaceId by remember { mutableStateOf<String?>(null) }


    var appTarget by remember { mutableStateOf<AppModel?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }



    LaunchedEffect(autoShowKeyboard) {
        if (autoShowKeyboard) {
            yield()
            focusRequester.requestFocus()
        }
    }


    /**
     * Updates the visible workspace
     */
    LaunchedEffect(visibleWorkspaces, selectedWorkspaceId) {
        if (visibleWorkspaces.isEmpty()) return@LaunchedEffect

        val selectedVisible = visibleWorkspaces.any { it.id == selectedWorkspaceId }
        val targetId = if (selectedVisible) selectedWorkspaceId else visibleWorkspaces.first().id
        val targetIndex = visibleWorkspaces.indexOfFirst { it.id == targetId }

        if (!selectedVisible) {
            appsViewModel.selectWorkspace(targetId)
        }

        if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    /**
     * Fires on workspace state change
     * launch the private space unlocking prompt if workspace type if private space
     */
    LaunchedEffect(pagerState.currentPage) {
        val newWorkspace =
            visibleWorkspaces.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        val newWorkspaceId = newWorkspace.id

        // Check if switching to Private Space (Android 15+)
        if (PrivateSpaceUtils.isPrivateSpaceSupported() &&
            newWorkspace.type == WorkspaceType.PRIVATE &&
            privateSpaceState.isLocked
        ) {
            privateSpaceViewModel.onUnlockPrivateSpace()
        }

        workspaceId = newWorkspaceId
        appsViewModel.selectWorkspace(newWorkspaceId)
    }


    fun closeKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun openKeyboard() {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun toggleKeyboard() {
        if (isSearchFocused) {
            closeKeyboard()
        } else {
            openKeyboard()
        }
    }


    fun launchDrawerAction(action: DrawerActions) {
        when (action) {
            CLOSE -> onClose()
            TOGGLE_KB -> toggleKeyboard()
            CLOSE_KB -> closeKeyboard()
            OPEN_KB -> openKeyboard()

            CLEAR -> searchQuery = ""
            SEARCH_WEB -> {
                if (searchQuery.isNotBlank()) ctx.openSearch(searchQuery)
            }

            OPEN_FIRST_APP -> haveToLaunchFirstApp = true
            NONE, DISABLED -> {}
        }
    }

    // Used to correctly handle the home action when in drawer (otherwise the action is consumed by the nav host and not made here)
    DisposableEffect(Unit) {

        val handler = {
            launchDrawerAction(drawerHomeAction)
        }

        onRegisterHomeHandler(handler)

        onDispose {
            onRegisterHomeHandler(null)
        }
    }

    BackHandler {
        launchDrawerAction(drawerBackAction)
    }


    val filteredToolbarsOrder by remember(drawerToolbarsOrder, showSearchBar, showRecentlyUsedApps) {
        derivedStateOf {
            drawerToolbarsOrder.filter { item ->
                when (item) {
                    RecentlyUsed -> showRecentlyUsedApps
                    SearchBar -> showSearchBar
                    else -> true
                }
            }
        }
    }

    // Computes the position of the spacer in the toolbars list, and deduce 2 lists:
    // one with the elements that come before, and one with those that come after
    val spacerIndex = remember(filteredToolbarsOrder) {
        filteredToolbarsOrder.indexOf(Spacer).takeIf { it != -1 } ?: 0
    }
    val beforeSpacer = remember(filteredToolbarsOrder, spacerIndex) {
        filteredToolbarsOrder.subList(0, spacerIndex)
    }
    val afterSpacer = remember(filteredToolbarsOrder, spacerIndex) {
        filteredToolbarsOrder.subList(spacerIndex + 1, filteredToolbarsOrder.size)
    }

    var searchBarHeightPx by remember { mutableIntStateOf(0) }
    var recentAppsHeightPx by remember { mutableIntStateOf(0) }


    val density = LocalDensity.current

    val appsContentPadding = remember(filteredToolbarsOrder, searchBarHeightPx, recentAppsHeightPx) {
        PaddingValues(
            top = with(density) {
                beforeSpacer.sumOf {
                    when (it) {
                        Spacer -> 0
                        RecentlyUsed -> recentAppsHeightPx
                        SearchBar -> searchBarHeightPx
                    }
                }.toDp() + 5.dp
            },
            bottom = with(density) {
                afterSpacer.sumOf {
                    when (it) {
                        Spacer -> 0
                        RecentlyUsed -> recentAppsHeightPx
                        SearchBar -> searchBarHeightPx
                    }
                }.toDp() + 5.dp
            }
        )
    }

//    logD(DRAWER_TAG) { "toolbar order: $drawerToolbarsOrder, filtered: $filteredToolbarsOrder SpacerIndex: $spacerIndex, beforeSpacer: $beforeSpacer, after: $afterSpacer\ntopPadding: $topPadding, bottomPadding: $bottomPadding" }

    /* ───────────── Pull Down System ───────────── */

    val pullDownAnimations by DrawerSettingsStore.pullDownAnimations.asState()
    val pullDownScaleIn by DrawerSettingsStore.pullDownScaleIn.asState()
//    val pullDownIconFade by DrawerSettingsStore.pullDownIconFade.asState()

    var atTop by remember { mutableStateOf(true) }

    val haptic = LocalHapticFeedback.current

    val thresholdPx = Constants.Drawer.DRAWER_DRAG_DOWN_THRESHOLD.dp.px
    val maxDragDownOffset = Constants.Drawer.DRAWER_MAX_DRAG_DOWN.dp.px

    var pullOffset by remember { mutableFloatStateOf(0f) }

    /**
     * `Of..1f`, used for animations
     * `1f` is at the threshold
     */
    val pullProgress = 1 - (pullOffset / thresholdPx).coerceAtMost(1f)

    /**
     * If the haptic feedback has already been executed, to avoid repeating it indefinitely
     */
    var hasHapticed by remember { mutableStateOf(false) }

    /**
     * The scroll state basically, defines what happen on vertical scrolls, the horizontal being handled by the pager
     * Responsible for the drag up/down actions, and the top padding of the drawer on down drag
     */
    val nestedConnection = remember {

        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {

                if (source != NestedScrollSource.UserInput)
                    return Offset.Zero

                // ignore horizontal gestures
                if (abs(available.y) <= abs(available.x))
                    return Offset.Zero

                // Down Drag (pull-to-trigger)
                if (available.y > 0f && atTop) {

                    // Linear curve for clean output
                    val newPullOffset = pullOffset + available.y * (1f - (pullOffset / thresholdPx))
                        .coerceAtLeast(0.2f)

                    // Block when max offset is reached (constant)
                    pullOffset = newPullOffset.coerceAtMost(maxDragDownOffset)

                    val thresholdReachedNow = pullOffset > thresholdPx

                    // Haptic feedback
                    if (thresholdReachedNow && !hasHapticed) {
                        hasHapticed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    if (!thresholdReachedNow && hasHapticed) hasHapticed = false

                    // consume only what we used
                    return Offset(0f, available.y)
                }

                // UP DRAG while stretching (reversible)
                if (available.y < 0f && pullOffset > 0f) {

                    pullOffset = (pullOffset + available.y).coerceAtLeast(0f)


                    if (!(pullOffset > thresholdPx) && hasHapticed) hasHapticed = false
                    return Offset(0f, available.y)
                }


                // Launch Up action on any up scroll large enough
                if (available.y < -15) {
                    launchDrawerAction(drawerScrollUpAction)
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {

                // No need to enclave in if statement as values aren't changing if !pullDownAnimations

                // DOWN action
                if (pullOffset > thresholdPx) {
                    launchDrawerAction(drawerScrollDownAction)
                }

                // reset
                pullOffset = 0f
                hasHapticed = false

                return Velocity.Zero
            }
        }
    }


    val animatedScale by animateFloatAsState(
        targetValue = if (pullDownScaleIn) (pullProgress.pow(0.9f)).coerceIn(0.95f, 1f)
        else 1f
    )


    val pullDownPadding = if (pullDownAnimations) pullOffset else 0f
    val animatedPadding by animateDpAsState(targetValue = pullDownPadding.toDp)

    @Composable
    fun AppLongPressRow(app: AppModel) {
        val cacheKey = app.iconCacheKey

        AppLongPressRow(
            app = app,
            onOpen = { onLaunchAction(app.action) },
            onSettings = if (!app.isPrivateProfile && !app.isWorkProfile) {
                {
                    ctx.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${app.packageName}".toUri()
                        }
                    )
                    onClose()
                }
            } else null,
            onUninstall = if (!app.isPrivateProfile && !app.isWorkProfile) {
                {
                    ctx.startActivity(
                        Intent(Intent.ACTION_DELETE).apply {
                            data = "package:${app.packageName}".toUri()
                        }
                    )
                    onClose()
                }
            } else null,
            onRemoveFromWorkspace = if (!app.isPrivateProfile) {
                {
                    workspaceId?.let { wsId ->
                        scope.launch {
                            appsViewModel.removeAppFromWorkspace(
                                workspaceId = wsId,
                                cacheKey = cacheKey
                            )
                        }
                    }
                }
            } else null,
            onRenameApp = { renameAppTarget = app },
            onChangeAppIcon = { appTarget = app },
            onAliases = { showAliasDialog = app }
        )
    }


    /* ───────────── Dim wallpaper system ───────────── */
    val wallpaperDimDrawerScreen by UiSettingsStore.wallpaperDimDrawerScreen.asState()
    val pullDownWallPaperDimFadeEnabled by DrawerSettingsStore.pullDownWallPaperDimFade.asState()

    val animatedDim by animateFloatAsState(targetValue = pullProgress)
    // Dims the wallpaper, when the user starts pulling down,
    // the dim amount is reduced proportionally to the drag amount
    val dimAmount = wallpaperDimDrawerScreen *
            if (pullDownWallPaperDimFadeEnabled) animatedDim
            else 1f

    WallpaperDim(dimAmount)


    /* ───────────── Main Content ───────────── */
    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedConnection)
                .padding(top = animatedPadding)
                .conditional(pullDownScaleIn) {
                    graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                }
                .clickable(
                    enabled = tapEmptySpaceToRaiseKeyboard.isUsed(),
                    indication = null,
                    interactionSource = null
                ) {
                    toggleKeyboard()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {

                if (leftAction != DISABLED) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(leftWeight.coerceIn(0.001f, 1f))
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) { launchDrawerAction(leftAction) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {

                    HorizontalPager(
                        state = pagerState,
                        key = { it.hashCode() }
                    ) { pageIndex ->

                        val workspace = visibleWorkspaces[pageIndex]

                        val gridState = remember(workspace.id) {
                            LazyGridState()
                        }

                        val categoryGridState = remember(workspace.id) {
                            LazyGridState()
                        }

                        val listState = remember(workspace.id) {
                            LazyListState()
                        }

                        val apps by appsViewModel
                            .appsForWorkspace(workspace, overrides)
                            .collectAsStateWithLifecycle(emptyList())

                        val filteredApps by remember(searchQuery, apps) {
                            derivedStateOf {
                                val trimmedSearchQuery = searchQuery.trim()

                                val base = if (trimmedSearchQuery.isBlank()) apps
                                else apps.filter { app ->
                                    app.name.contains(trimmedSearchQuery, ignoreCase = true) ||

                                            // Also search for aliases
                                            aliases[app.iconCacheKey]?.any {
                                                it.contains(
                                                    trimmedSearchQuery,
                                                    ignoreCase = true
                                                )
                                            } ?: false
                                }

                                base.sortedBy { it.name.lowercase() }
                            }
                        }

                        LaunchedEffect(haveToLaunchFirstApp, filteredApps) {

                            val autoLaunch =
                                autoLaunchSingleMatch &&
                                        filteredApps.size == 1 &&
                                        searchQuery.isNotEmpty() &&
                                        !(disableAutoLaunchOnSpaceFirstChar && searchQuery.first() == ' ')

                            if (haveToLaunchFirstApp || autoLaunch && filteredApps.isNotEmpty()) {
                                onLaunchAction(filteredApps.first().action)
                            }
                        }

                        // If the current workspace is a private space and locked, display a lock icon
                        val showLock =
                            privateSpaceState.isLocked || privateSpaceState.isAuthenticating

                        if (workspace.type == WorkspaceType.PRIVATE && showLock) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    // Just so that the scroll actions are registered
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(targetState = privateSpaceState) {
                                    when {
                                        // The loading shouldn't be displayed, but just in case I'll keep it for user visual feedback
                                        it.isLoading -> LoadingIndicator()
                                        it.isAuthenticating -> LoadingIndicator(color = Color.Yellow)
                                        it.isLocked -> {
                                            DragonIconButton(
                                                icon = R.drawable.lock,
                                                contentDescription = stringResource(R.string.private_space_locked)
                                            ) { privateSpaceViewModel.onUnlockPrivateSpace() }
                                        }
                                    }
                                }
                            }
                        } else {

                            AppGrid(
                                apps = filteredApps,
                                gridState = gridState,
                                paddingValues = appsContentPadding,
                                categoryGridState = categoryGridState,
                                listState = listState,
                                onTopStateChange = { atTop = it },
                                onReload = {
                                    scope.launch {
                                        if (workspace.type == WorkspaceType.PRIVATE) appsViewModel.unlockAndReloadPrivateSpace()
                                        else appsViewModel.reloadApps()
                                    }
                                },
                                longPressPopup = { app -> AppLongPressRow(app) }
                            ) {
                                onLaunchAction(it.action)
                            }
                        }
                    }
                }

                if (rightAction != DISABLED) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(rightWeight.coerceIn(0.001f, 1f))
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) { launchDrawerAction(rightAction) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {

            drawerToolbarsOrder.forEach { toolbar ->
                when (toolbar) {
                    Spacer -> Spacer(Modifier.weight(1f))

                    RecentlyUsed -> {
                        /* ───────────── Recently Used Apps section ───────────── */
                        AnimatedVisibility(
                            visible = showRecentlyUsedApps && searchQuery.isBlank() && recentApps.isNotEmpty(),
                            modifier = Modifier.onGloballyPositioned {
                                recentAppsHeightPx = it.size.height
                            }
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                                    .settingsGroup()
                            ) {
                                AppGrid(
                                    apps = recentApps,
                                    fillMaxSize = false,
                                    longPressPopup = { app -> AppLongPressRow(app) }
                                ) {
                                    onLaunchAction(it.action)
                                }
                            }
                        }
                    }

                    SearchBar -> {
                        AnimatedVisibility(
                            visible = showSearchBar,
                            modifier = Modifier.onGloballyPositioned {
                                searchBarHeightPx = it.size.height
                            }
                        ) {
                            AppDrawerSearch(
                                searchQuery = searchQuery,
                                trailingIcon = {
                                    Box {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = stringResource(R.string.more),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.shapedClickable { showMoreMenu = true }
                                        )

                                        BurgerListAction(
                                            actions = listOf(
                                                MoreOptions(
                                                    onClick = { onNavigate(NavigationRoute.DrawerSettings) },
                                                    icon = R.drawable.ic_action_drawer,
                                                    text = { stringResource(R.string.drawer_settings) }
                                                )
                                            ),
                                            isExpanded = showMoreMenu,
                                            onDismissRequest = { showMoreMenu = false }
                                        )
                                    }
                                },
                                onSearchChanged = { searchQuery = it },
                                modifier = Modifier.focusRequester(focusRequester),
                                onEnterPressed = { launchDrawerAction(drawerEnterAction) },
                                onFocusStateChanged = { isSearchFocused = it }
                            )
                        }
                    }
                }
            }
        }
    }



    if (renameAppTarget != null) {
        val app = renameAppTarget!!
        val cacheKey = app.iconCacheKey

        TextEditorDialog(
            title = { stringResource(R.string.rename) },
            placeHolder = { app.name },
            onDismiss = { renameAppTarget = null },
            initialText = app.name
        ) {
            if (it != "") {
                appsViewModel.renameApp(
                    cacheKey = cacheKey,
                    customName = it
                )
            } else {
                appsViewModel.resetAppName(cacheKey)
            }
            renameAppTarget = null
        }
    }

    if (appTarget != null) {

        val app = appTarget!!
        val cacheKey = app.iconCacheKey

        AppIconEditor(
            app = app,
            onReset = { appsViewModel.reloadAppIcon(app) },
            onDismiss = { appTarget = null }
        ) { customIcon ->

            scope.launch {
                if (customIcon != null) {
                    appsViewModel.setAppIcon(
                        cacheKey = cacheKey,
                        customIcon = customIcon
                    )
                } else {
                    appsViewModel.resetAppIcon(cacheKey)
                }
                appsViewModel.reloadAppIcon(app)
                appTarget = null
            }
        }
    }

    if (showAliasDialog != null) {
        val app = showAliasDialog!!

        AppAliasesDialog(
            app = app,
            onDismiss = { showAliasDialog = null }
        )
    }
}
