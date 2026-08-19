package org.elnix.dragonlauncher.ui.drawer

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.base.Constants
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Clear
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Close
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.CloseKb
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Companion.isUsed
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.Disabled
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.None
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.OpenFirstApp
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.OpenKb
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.SearchWeb
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.ToggleKb
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.RecentlyUsed
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.SearchBar
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.Spacer
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.openSearch
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.helpers.wallpaper.WallpaperDim
import org.elnix.dragonlauncher.ui.helpers.workspace.AppDrawerSearch
import org.elnix.dragonlauncher.ui.helpers.workspace.AppGrid
import org.elnix.dragonlauncher.ui.helpers.workspace.WorkspaceLockedContent
import org.elnix.dragonlauncher.ui.helpers.workspace.WorkspaceUnavailableContent
import kotlin.math.abs
import kotlin.math.pow

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDrawerScreen(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    profilesViewModel: ProfilesViewModel = activityViewModel(),
    onRegisterHomeHandler: ((() -> Unit)?) -> Unit,
    onLaunchAction: (Action) -> Unit
) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val drawerSettings = LocalDrawerSettings.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }

    val autoShowKeyboard  = drawerSettings.autoShowKeyboard
    val showSearchBar  = drawerSettings.showSearchBar
    val showRecentlyUsedApps  = drawerSettings.showRecentlyUsedApps
    val toolbarsOrder  = drawerSettings.toolbarsOrder

    val recentApps by drawerViewModel.getRecentApps(drawerSettings.recentlyUsedAppsCount).collectAsStateWithLifecycle(emptyList())


    var haveToLaunchFirstApp by remember { mutableStateOf(false) }
    var searchQuery by drawerViewModel.searchQuery
    var isSearchFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit, autoShowKeyboard) {
        if (autoShowKeyboard) {
            yield()
            focusRequester.requestFocus()
        }
    }


    val activeWorkspaces by drawerViewModel.activeWorkspaces.collectAsStateWithLifecycle()
    val selectedWorkspaceId by drawerViewModel.selectedWorkspaceId.collectAsStateWithLifecycle()

    val initialIndex = activeWorkspaces.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (activeWorkspaces.size - 1).coerceAtLeast(0)),
        pageCount = { activeWorkspaces.size }
    )

    /**
     * Updates the visible workspace
     */
    LaunchedEffect(activeWorkspaces, selectedWorkspaceId) {
        if (activeWorkspaces.isEmpty()) return@LaunchedEffect

        val selectedPresent = activeWorkspaces.any { it.id == selectedWorkspaceId }
        val targetId = if (selectedPresent) selectedWorkspaceId else activeWorkspaces.first().id
        val targetIndex = activeWorkspaces.indexOfFirst { it.id == targetId }

        if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val newWorkspace = if (activeWorkspaces.size > pagerState.currentPage) activeWorkspaces[pagerState.currentPage] else null
        val targetId = newWorkspace?.id ?: activeWorkspaces.firstOrNull()?.id
        DrawerSettingsStore.lastWorkspaceUsed.set(ctx, targetId)
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
            Close -> navigator.onBack()
            ToggleKb -> toggleKeyboard()
            CloseKb -> closeKeyboard()
            OpenKb -> openKeyboard()

            Clear -> searchQuery = ""
            SearchWeb -> {
                if (searchQuery.isNotBlank()) ctx.openSearch(searchQuery)
            }

            OpenFirstApp -> haveToLaunchFirstApp = true
            None, Disabled -> {}
        }
    }

    // Used to correctly handle the home action when in drawer (otherwise the action is consumed by the nav host and not made here)
    DisposableEffect(Unit) {

        val handler = {
            launchDrawerAction(drawerSettings.drawerHomeAction)
        }

        onRegisterHomeHandler(handler)

        onDispose {
            onRegisterHomeHandler(null)
        }
    }

    BackHandler {
        launchDrawerAction(drawerSettings.drawerBackAction)
    }


    val filteredToolbarsOrder by remember(toolbarsOrder, showSearchBar, showRecentlyUsedApps) {
        derivedStateOf {
            toolbarsOrder.filter { item ->
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


    val pullDownAnimations by DrawerSettingsStore.pullDownAnimations.asState()
    val pullDownScaleIn by DrawerSettingsStore.pullDownScaleIn.asState()
//    val pullDownIconFade by DrawerSettingsStore.pullDownIconFade.asState()

    var atTop by remember { mutableStateOf(true) }


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
                    launchDrawerAction(drawerSettings.drawerScrollUpAction)
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
                    launchDrawerAction(drawerSettings.drawerScrollDownAction)
                }

                // reset
                pullOffset = 0f
                hasHapticed = false

                return Velocity.Zero
            }
        }
    }

    val wallpaperDimDrawerScreen by UiSettingsStore.wallpaperDimDrawerScreen.asState()
    val wallpaperDimMainScreen by UiSettingsStore.wallpaperDimMainScreen.asState()
    val pullDownWallPaperDimFadeEnabled by DrawerSettingsStore.pullDownWallPaperDim.asState()

    val leftDrawerAction by DrawerSettingsStore.leftDrawerAction.asState()
    val leftDrawerWidth by DrawerSettingsStore.leftDrawerWidth.asState()

    val rightDrawerAction by DrawerSettingsStore.rightDrawerAction.asState()
    val rightDrawerWidth by DrawerSettingsStore.rightDrawerWidth.asState()

    val profiles by profilesViewModel.profiles.collectAsState(emptyList())
    val profileStates by profilesViewModel.profileStates.collectAsState(emptyList())


    val animatedScale by animateFloatAsState(if (pullDownScaleIn) (pullProgress.pow(0.9f)).coerceIn(0.95f, 1f) else 1f)
    val animatedPadding by animateDpAsState((if (pullDownAnimations) pullOffset else 0f).toDp)

    val dim = remember(pullProgress, pullDownWallPaperDimFadeEnabled, wallpaperDimDrawerScreen, wallpaperDimMainScreen) {
       if (pullDownWallPaperDimFadeEnabled) {
           wallpaperDimDrawerScreen + pullProgress * (wallpaperDimDrawerScreen - wallpaperDimMainScreen)
       } else wallpaperDimDrawerScreen
    }

    // Dims the wallpaper, when the user starts pulling down,
    // the dim amount is reduced proportionally to the drag amount
    WallpaperDim(dim)


    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime))
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
                enabled = drawerSettings.tapEmptySpaceAction.isUsed,
                indication = null,
                interactionSource = null
            ) {
                toggleKeyboard()
            }
    ) {
        DrawerActions(leftDrawerAction, leftDrawerWidth, rightDrawerAction, rightDrawerWidth, ::launchDrawerAction)
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = leftDrawerWidth, end = rightDrawerWidth),
            state = pagerState
        ) { pageIndex ->

            val workspace = activeWorkspaces[pageIndex]

            val workspaceProfileType = when (workspace.type) {
                WorkspaceType.Work -> Profile.Type.Work
                WorkspaceType.Private -> Profile.Type.Private
                else -> Profile.Type.Personal
            }

            val workspaceProfile = profiles.find { it?.type == workspaceProfileType }

            val workspaceLocked = when (workspaceProfileType) {
                Profile.Type.Personal -> false
                Profile.Type.Work -> profileStates.getOrNull(1)?.locked ?: true
                Profile.Type.Private -> profileStates.getOrNull(2)?.locked ?: true
            }

            val gridState = remember(workspace.id) { LazyGridState() }
            val listState = remember(workspace.id) { LazyListState() }
            val categoryGridState = remember(workspace.id) { LazyGridState() }

            val apps by drawerViewModel.search(workspace).collectAsStateWithLifecycle()


            LaunchedEffect(haveToLaunchFirstApp, apps) {

                val autoLaunch =
                    drawerSettings.autoOpenSingleMatch &&
                            apps.size == 1 &&
                            searchQuery.isNotEmpty() &&
                            !(drawerSettings.disableAutoLaunchWhenFirstCharIs.isNotEmpty() && searchQuery.first() == drawerSettings.disableAutoLaunchWhenFirstCharIs.first())

                if (haveToLaunchFirstApp || autoLaunch && apps.isNotEmpty()) {
                    onLaunchAction(apps.first().action)
                }
            }

            when {
                workspaceProfile == null -> {
                    WorkspaceUnavailableContent(workspace.type)
                }

                workspaceLocked -> {
                    WorkspaceLockedContent(
                        workspaceProfile = workspaceProfile,
                        isActive = selectedWorkspaceId == workspace.id
                    )
                }

                else -> {
                    AppGrid(
                        apps = apps,
                        gridState = gridState,
                        paddingValues = appsContentPadding,
                        categoryGridState = categoryGridState,
                        listState = listState,
                        onTopStateChange = { atTop = it },
                        longPressPopup = true
                    ) {
                        onLaunchAction(it.action)
                    }
                }
            }
        }
    }


    /**
     * Toolbars column, fills the whole size and sits over the apps boxes
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {

        var showMoreMenu by remember { mutableStateOf(false) }

        toolbarsOrder.forEach { toolbar ->
            when (toolbar) {
                Spacer -> Spacer(Modifier.weight(1f))

                RecentlyUsed -> {
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
                                longPressPopup = true,
                                onReload = drawerViewModel::reloadApps
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

                                    val navigator = LocalNavigator.current
                                    BurgerListAction(
                                        actions = listOf(
                                            MoreOptions(
                                                onClick = { navigator.navigate(NavigationRoute.DrawerSettings) },
                                                icon = R.drawable.workspaces,
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
                            onEnterPressed = { launchDrawerAction(drawerSettings.drawerEnterAction) },
                            onFocusStateChanged = { isSearchFocused = it }
                        )
                    }
                }
            }
        }
    }
}


/**
 * Drawer actions, creates left and right clickable buttons that can activate the selected [org.elnix.dragonlauncher.enumsui.toggle.DrawerActions]
 */
@Composable
fun BoxScope.DrawerActions(
    leftDrawerAction: DrawerActions,
    leftDrawerWidth: Dp,
    rightDrawerAction: DrawerActions,
    rightDrawerWidth: Dp,
    launchDrawerAction: (DrawerActions) -> Unit
) {
    if (leftDrawerAction != Disabled) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(leftDrawerWidth)
                .clickable(
                    indication = null,
                    interactionSource = null
                ) { launchDrawerAction(leftDrawerAction) }
        )
    }

    if (rightDrawerAction != Disabled) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(rightDrawerWidth)
                .clickable(
                    indication = null,
                    interactionSource = null
                ) { launchDrawerAction(rightDrawerAction) }
        )
    }
}