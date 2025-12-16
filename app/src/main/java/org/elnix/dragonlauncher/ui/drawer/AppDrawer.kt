package org.elnix.dragonlauncher.ui.drawer

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.data.helpers.DrawerActions
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.utils.models.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.ImageUtils
import org.elnix.dragonlauncher.utils.actions.launchSwipeAction
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppDrawerScreen(
    appsViewModel: AppDrawerViewModel,
    workspaceViewModel: WorkspaceViewModel,
    showIcons: Boolean,
    showLabels: Boolean,
    autoShowKeyboard: Boolean,
    gridSize: Int,
    searchBarBottom: Boolean,
    leftAction: DrawerActions,
    leftWidth: Float,
    rightAction: DrawerActions,
    rightWidth: Float,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current

    val workspaceState by workspaceViewModel.enabledState.collectAsState()
    val workspaces = workspaceState.workspaces
    val overrides = workspaceState.appOverrides

    val selectedWorkspaceId by workspaceViewModel.selectedWorkspaceId.collectAsState()
    val initialIndex = workspaces.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (workspaces.size - 1).coerceAtLeast(0)),
        pageCount = { workspaces.size }
    )

    val icons by appsViewModel.icons.collectAsState()

    val autoLaunchSingleMatch by DrawerSettingsStore
        .getAutoLaunchSingleMatch(ctx)
        .collectAsState(initial = true)

    val clickEmptySpaceToRaiseKeyboard by DrawerSettingsStore
        .getClickEmptySpaceToRaiseKeyboard(ctx)
        .collectAsState(initial = false)

    var searchQuery by remember { mutableStateOf("") }
    var dialogApp by remember { mutableStateOf<AppModel?>(null) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }

    LaunchedEffect(autoShowKeyboard) {
        if (autoShowKeyboard) {
            yield()
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val workspaceId = workspaces.getOrNull(pagerState.currentPage)?.id ?: return@LaunchedEffect
        workspaceViewModel.selectWorkspace(workspaceId)
    }


    fun toggleKeyboard() {
        if (isSearchFocused) {
            focusManager.clearFocus()
            keyboardController?.hide()
        } else {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    fun launchDrawerAction(action: DrawerActions) {
        when (action) {
            DrawerActions.CLOSE -> onClose()
            DrawerActions.TOGGLE_KB -> toggleKeyboard()
            DrawerActions.NONE -> Unit
            DrawerActions.DISABLED -> Unit
        }
    }

    @Composable
    fun DrawerTextInput() {
        AppDrawerSearch(
            searchQuery = searchQuery,
            onSearchChanged = { searchQuery = it },
            modifier = Modifier.focusRequester(focusRequester),
            onEnterPressed = { searchQuery = "" },
            onFocusStateChanged = { isSearchFocused = it }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                enabled = clickEmptySpaceToRaiseKeyboard,
                indication = null,
                interactionSource = null
            ) { toggleKeyboard() }
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {

        if (!searchBarBottom) {
            DrawerTextInput()
        }

        Row(modifier = Modifier.fillMaxSize()) {

            if (leftAction != DrawerActions.DISABLED) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(leftWidth)
                        .clickable { launchDrawerAction(leftAction) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {

                HorizontalPager(state = pagerState) { pageIndex ->

                    val workspace = workspaces[pageIndex]

                    val apps by appsViewModel
                        .appsForWorkspace(workspace, overrides)
                        .collectAsState(initial = emptyList())

                    val filteredApps by remember(searchQuery, apps) {
                        derivedStateOf {
                            if (searchQuery.isBlank()) apps
                            else apps.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }
                        }
                    }

                    val iconsMerged = icons.toMutableMap()
                    apps.forEach { app ->
                        val base64 = overrides[app.packageName]?.customIconBase64
                        if (base64 != null) {
                            val bmp = ImageUtils.base64ToImageBitmap(base64)
                            if (bmp != null) iconsMerged[app.packageName] = bmp
                        }
                    }

                    LaunchedEffect(filteredApps) {
                        if (autoLaunchSingleMatch && filteredApps.size == 1 && searchQuery.isNotEmpty()) {
                            launchSwipeAction(ctx, filteredApps.first().action)
                            onClose()
                        }
                    }

                    AppGrid(
                        apps = filteredApps,
                        icons = iconsMerged,
                        gridSize = gridSize,
                        txtColor = MaterialTheme.colorScheme.onSurface,
                        showIcons = showIcons,
                        showLabels = showLabels,
                        onLongClick = { dialogApp = it }
                    ) {
                        launchSwipeAction(ctx, it.action)
                        onClose()
                    }
                }
            }

            if (rightAction != DrawerActions.DISABLED) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(rightWidth)
                        .clickable { launchDrawerAction(rightAction) }
                )
            }
        }
    }

    if (dialogApp != null) {
        val app = dialogApp!!
        AppLongPressDialog(
            app = app,
            onDismiss = { dialogApp = null },
            onOpen = {
                launchSwipeAction(ctx, app.action)
                onClose()
            },
            onSettings = {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${app.packageName}".toUri()
                    }
                )
                onClose()
            },
            onUninstall = {
                ctx.startActivity(
                    Intent(Intent.ACTION_DELETE).apply {
                        data = "package:${app.packageName}".toUri()
                    }
                )
                onClose()
            }
        )
    }
}




@Composable
fun AppDrawerSearch(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onEnterPressed: () -> Unit = {},
    onFocusStateChanged: (Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = searchQuery,
        onValueChange = onSearchChanged,
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .onFocusChanged { focusState ->
                val focused = focusState.isFocused
                onFocusStateChanged(focused) // Notify parent of focus change
                if (focused) {
                    keyboardController?.show() // Show keyboard when TextField gains focus
                }
                // Keyboard hiding on focus loss is handled by system, IME actions, or explicit calls elsewhere (e.g., scroll logic)
            },
        placeholder = { Text("Search apps...") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            // Don't hide the keyboard on enter, just clear the search
//            keyboardController?.hide() // Hide keyboard on IME "Search" action
            onEnterPressed()
        }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}
