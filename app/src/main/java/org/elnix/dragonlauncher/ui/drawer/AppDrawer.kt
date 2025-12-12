package org.elnix.dragonlauncher.ui.drawer

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.actions.launchSwipeAction

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppDrawerScreen(
    appsViewModel: AppDrawerViewModel,
    initialPage: Int,
    showIcons: Boolean,
    showLabels: Boolean,
    autoShowKeyboard: Boolean,
    gridSize: Int,
    searchBarBottom: Boolean,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val userApps by appsViewModel.userApps.collectAsState()
    val workApps by appsViewModel.workProfileApps.collectAsState()
    val systemApps by appsViewModel.systemApps.collectAsState()
    val allApps by appsViewModel.allApps.collectAsState()

    val pages = mutableListOf("User", "System", "All")
    if (workApps.isNotEmpty()) {
        pages.add(1, "Work")
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pages.size }
    )


    val icons by appsViewModel.icons.collectAsState()

    val autoLaunchSingleMatch by DrawerSettingsStore.getAutoLaunchSingleMatch(ctx)
        .collectAsState(initial = true)

    val clickEmptySpaceToRaiseKeyboard by DrawerSettingsStore.getClickEmptySpaceToRaiseKeyboard(ctx)
        .collectAsState(initial = false)


    var searchQuery by remember { mutableStateOf("") }
    var dialogApp by remember { mutableStateOf<AppModel?>(null) }

    val filteredUser by remember(searchQuery) {
        derivedStateOf { userApps.filter { it.name.contains(searchQuery, ignoreCase = true) } }
    }
    val filteredAll by remember(searchQuery) {
        derivedStateOf { allApps.filter { it.name.contains(searchQuery, ignoreCase = true) } }
    }
    val filteredSystem by remember(searchQuery) {
        derivedStateOf { systemApps.filter { it.name.contains(searchQuery, ignoreCase = true) } }
    }
    val filteredWork by remember(searchQuery) {
        derivedStateOf { workApps.filter { it.name.contains(searchQuery, ignoreCase = true) } }
    }


    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchFocused by remember { mutableStateOf(false) }


    LaunchedEffect(focusRequester) {
        if (autoShowKeyboard) {
            yield()
            focusRequester.requestFocus()
        }
    }

    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollState, keyboardController, focusManager, focusRequester, isSearchFocused) {
        var previousIndex = scrollState.firstVisibleItemIndex
        var previousOffset = scrollState.firstVisibleItemScrollOffset

        snapshotFlow {
            Triple(
                scrollState.firstVisibleItemIndex,
                scrollState.firstVisibleItemScrollOffset,
                scrollState.isScrollInProgress
            )
        }.collect { (currentIndex, currentOffset, isScrolling) ->
            if (isScrolling) {
                val actualScrollHappened = currentIndex != previousIndex || currentOffset != previousOffset
                if (actualScrollHappened) {
                    // Determine scroll direction: positive for down, negative for up
                    val verticalScrollDelta: Int = if (currentIndex > previousIndex) 1 // Major scroll down
                    else if (currentIndex < previousIndex) -1 // Major scroll up
                    else currentOffset - previousOffset // Minor scroll in same item

                    if (verticalScrollDelta > 0) { // User scrolled DOWN (content moved UP)
                        if (isSearchFocused) {
                            focusManager.clearFocus() // Will trigger onFocusStateChanged(false)
                        }
                        keyboardController?.hide()
                    } else { // User scrolled up
                        if (currentIndex == 0 && currentOffset == 0) { // Reached the very top of the list
                            if (!isSearchFocused) {
                                focusRequester.requestFocus() // Will trigger onFocusStateChanged(true) & show keyboard
                            }
                        }
                    }
                }
            }
            previousIndex = currentIndex
            previousOffset = currentOffset
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        scope.launch {
            DrawerSettingsStore.setInitialPage(ctx, pagerState.currentPage)
        }
    }

    LaunchedEffect(filteredUser, filteredSystem, filteredAll) {
        if (autoLaunchSingleMatch) {
            when (pagerState.currentPage) {
                pages.indexOf("User") -> {
                    if (filteredUser.size == 1) {
                        launchSwipeAction(ctx, filteredUser.first().action)
                        onClose()
                    }
                }
                pages.indexOf("System") -> {
                    if (filteredSystem.size == 1) {
                        launchSwipeAction(ctx, filteredSystem.first().action)
                        onClose()
                    }
                }
                pages.indexOf("All") -> {
                    if (filteredAll.size == 1) {
                        launchSwipeAction(ctx, filteredAll.first().action)
                        onClose()
                    }
                }
                pages.indexOf("Work") -> {
                    if (filteredWork.size == 1) {
                        launchSwipeAction(ctx, filteredWork.first().action)
                        onClose()
                    }
                }
            }
        }
    }


    @Composable
    fun DrawerTextInput() {
        AppDrawerSearch(
            searchQuery = searchQuery,
            onSearchChanged = { query -> searchQuery = query },
            modifier = Modifier.focusRequester(focusRequester),
            onEnterPressed = {
                // Clears the search query, cuz why not, its for efficiency first
                searchQuery = ""
            },
            onFocusStateChanged = { focused ->
                isSearchFocused = focused
                // Keyboard visibility is handled by onFocusChanged in AppDrawerSearch for focus gain,
                // and by scroll logic or IME actions for focus loss/hide.
            }
        )
    }


    Row (
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.8f)
                .clickable(
                    enabled = clickEmptySpaceToRaiseKeyboard,
                    indication = null,
                    interactionSource = null
                ) {
                    if (!isSearchFocused) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    } else {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
                .background(MaterialTheme.colorScheme.background)
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(15.dp)
        ) {


            if (!searchBarBottom) {
                DrawerTextInput()
            }

            HorizontalPager(
                state = pagerState
            ) { pageIndex ->

                val list = when (pageIndex) {
                    0 -> filteredUser
                    pages.indexOf("Work").takeIf { it > 0 } -> filteredWork
                    pages.indexOf("System") -> filteredSystem
                    else -> filteredAll
                }

                AppGrid(
                    apps = list,
                    icons = icons,
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

//        if (searchBarBottom) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .imePadding(),
//                contentAlignment = Alignment.BottomCenter
//            ) {
//                DrawerTextInput()
//            }
//        }
        }
    }

//    if (searchBarBottom) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .imePadding(),
//            contentAlignment = Alignment.BottomCenter
//        ) {
//            DrawerTextInput()
//        }
//    }

    if (dialogApp != null) {
        val app = dialogApp!!
        AppLongPressDialog(
            app = app,
            onDismiss = {
                dialogApp = null
            },
            onOpen = {
                launchSwipeAction(ctx, app.action)
                onClose()
            },
            onSettings = {
                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${app.packageName}".toUri()
                }
                ctx.startActivity(i)
                onClose()
            },
            onUninstall = {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = "package:${app.packageName}".toUri()
                }
                ctx.startActivity(intent)
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
