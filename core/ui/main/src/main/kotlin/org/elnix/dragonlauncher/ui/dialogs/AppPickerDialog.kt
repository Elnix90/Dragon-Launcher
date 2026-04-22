package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.WorkspaceType
import org.elnix.dragonlauncher.common.utils.Constants.Logging.PRIVATE_SPACE_TAG
import org.elnix.dragonlauncher.common.utils.PrivateSpaceUtils
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.composition.LocalAppLifecycleViewModel
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.AppDrawerSearch
import org.elnix.dragonlauncher.ui.helpers.AppGrid

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppPickerDialog(
    multiSelectEnabled: Boolean = false,
    onDismiss: () -> Unit,
    onAppSelected: (AppModel) -> Unit,
    onMultipleAppsSelected: ((List<AppModel>, Boolean) -> Unit)? = null
) {

    val appsViewModel = LocalAppsViewModel.current
    val appLifecycleViewModel = LocalAppLifecycleViewModel.current

    val privateSpaceState by appsViewModel.privateSpaceState.collectAsState()

    // Auto Show keyboard logic
    val focusRequester = remember { FocusRequester() }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchBarEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(isSearchBarEnabled) {
        if (isSearchBarEnabled) {
            yield()
            focusRequester.requestFocus()
        }
    }


    val workspaceState by appsViewModel.enabledState.collectAsState()
    val workspaces = workspaceState.workspaces
    val overrides = workspaceState.appOverrides
    val aliases = workspaceState.appAliases


    val selectedWorkspaceId by appsViewModel.selectedWorkspaceId.collectAsState()
    val initialIndex = workspaces.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (workspaces.size - 1).coerceAtLeast(0)),
        pageCount = { workspaces.size }
    )

    val scope = rememberCoroutineScope()

    // Multi-select state
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedApps = remember { mutableStateListOf<String>() }

    LaunchedEffect(pagerState.currentPage) {
        val newWorkspace =
            workspaces.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        val newWorkspaceId = newWorkspace.id

        // Check if switching to Private Space (Android 15+)
        if (PrivateSpaceUtils.isPrivateSpaceSupported() &&
            newWorkspace.type == WorkspaceType.PRIVATE &&
            privateSpaceState.isLocked
        ) {
            logW(PRIVATE_SPACE_TAG) { "Picker launch!" }
            appLifecycleViewModel.onUnlockPrivateSpace()
        }

        appsViewModel.selectWorkspace(newWorkspaceId)
    }

    CustomAlertDialog(
        alignment = Alignment.Center,
        modifier = Modifier
            .padding(15.dp)
            .height(700.dp),
        onDismissRequest = {
            if (isMultiSelectMode) {
                isMultiSelectMode = false
                selectedApps.clear()
            } else {
                onDismiss()
            }
        },
        scroll = false,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                AnimatedContent(
                    targetState = isSearchBarEnabled
                ) { searchBarDisplayed ->

                    if (!searchBarDisplayed) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = if (isMultiSelectMode)
                                    stringResource(R.string.multi_select_count, selectedApps.size)
                                else
                                    stringResource(R.string.select_app),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            AnimatedVisibility(isMultiSelectMode) {
                                DragonIconButton(
                                    onClick = {
                                        isMultiSelectMode = false
                                        selectedApps.clear()
                                    },
                                    colors = AppObjectsColors.iconButtonColors(),
                                    imageVector = Icons.Default.Deselect,
                                    contentDescription = stringResource(R.string.deselect_all)
                                )
                            }

                            DragonIconButton(
                                onClick = { isSearchBarEnabled = true },
                                colors = AppObjectsColors.iconButtonColors(),
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_apps)
                            )

                            DragonIconButton(
                                onClick = { scope.launch { appsViewModel.reloadApps() } },
                                colors = AppObjectsColors.iconButtonColors(),
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = stringResource(R.string.reload_apps)
                            )
                        }
                    } else {
                        AppDrawerSearch(
                            searchQuery = searchQuery,
                            onSearchChanged = { searchQuery = it },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.close_kb),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clickable {
                                        isSearchBarEnabled = false
                                        searchQuery = ""
                                    }
                                )
                            },
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                    }
                }


                Spacer(Modifier.height(6.dp))

                val listState = rememberLazyListState()

                LaunchedEffect(pagerState.currentPage) {
                    listState.animateScrollToItem(pagerState.currentPage)
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    itemsIndexed(workspaces) { index, workspace ->
                        val selected = pagerState.currentPage == index

                        val animatedColor by animateColorAsState(
                            if (selected)
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )

                        TextButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier.padding(5.dp),
                            shapes = UiConstants.dragonShapes(),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = animatedColor
                            )
                        ) {
                            Text(
                                text = workspace.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }


                // Multi-select hint
                AnimatedVisibility(multiSelectEnabled && !isMultiSelectMode) {
                    Text(
                        text = stringResource(R.string.multi_select_drawer_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->

                    val workspace = workspaces[pageIndex]

                    val apps by appsViewModel
                        .appsForWorkspace(workspace, overrides)
                        .collectAsState(initial = emptyList())

                    val filteredApps by remember(searchQuery, apps) {
                        derivedStateOf {
                            val base = if (searchQuery.isBlank()) apps
                            else apps.filter { app ->
                                app.name.contains(searchQuery, ignoreCase = true) ||

                                        // Also search for aliases
                                        aliases[app.iconCacheKey]?.any {
                                            it.contains(
                                                searchQuery,
                                                ignoreCase = true
                                            )
                                        } ?: false
                            }

                            base.sortedBy { it.name.lowercase() }
                        }
                    }


                    val showLock =
                        privateSpaceState.isLocked || privateSpaceState.isAuthenticating

                    if (workspace.type == WorkspaceType.PRIVATE && showLock) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(targetState = privateSpaceState) {
                                when {
                                    // The loading shouldn't be displayed, but just in case I'll keep it for user visual feedback
                                    it.isLoading -> LoadingIndicator()
                                    it.isAuthenticating -> LoadingIndicator(color = Color.Yellow)
                                    it.isLocked -> {
                                        DragonIconButton(
                                            onClick = {
                                                logW(PRIVATE_SPACE_TAG) { "Drawer reload button launch!" }
                                                appLifecycleViewModel.onUnlockPrivateSpace()
                                            },
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Private Space Locked"
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        AppGrid(
                            apps = filteredApps,
                            selectedPackages = selectedApps,
                            isMultiSelectMode = isMultiSelectMode,
                            onReload = {
                                scope.launch {
                                    if (workspace.type == WorkspaceType.PRIVATE) appsViewModel.unlockAndReloadPrivateSpace()
                                    else appsViewModel.reloadApps()
                                }
                            },
                            onEnterMultiSelect = { app ->
                                isMultiSelectMode = true
                                if (!selectedApps.contains(app.packageName)) {
                                    selectedApps.add(app.packageName)
                                }
                            },
                            onToggleSelect = { app ->
                                if (selectedApps.contains(app.packageName)) {
                                    selectedApps.remove(app.packageName)
                                } else {
                                    selectedApps.add(app.packageName)
                                }
                                if (selectedApps.isEmpty()) {
                                    isMultiSelectMode = false
                                }
                            },
                            longPressPopup = null,
                            onClick = {
                                onAppSelected(it)
                                onDismiss()
                            }
                        )
                    }
                }

                // Multi-select action bar
                AnimatedVisibility(isMultiSelectMode && selectedApps.isNotEmpty() && onMultipleAppsSelected != null) {
                    if (onMultipleAppsSelected != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .settingsGroup(border = true),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Resolve picked apps to AppModel list
                            Button(
                                onClick = {
//                                val workspaceApps = workspaces.flatMap { ws ->
//                                    val flow = appsViewModel.appsForWorkspace(ws, overrides)
//                                    // we'll resolve via getAllApps from ViewModel
//                                    emptyList<AppModel>()
//                                }
                                    // We'll use the allApps state reference
                                    val allApps = appsViewModel.allApps.value
                                    val pickedApps = allApps.filter { it.packageName in selectedApps }
                                    onMultipleAppsSelected(pickedApps, true)
                                    onDismiss()
                                },
                                shape = DragonShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.add_all_auto))
                            }

                            Button(
                                onClick = {
                                    val allApps = appsViewModel.allApps.value
                                    val pickedApps = allApps.filter { it.packageName in selectedApps }
                                    onMultipleAppsSelected(pickedApps, false)
                                    onDismiss()
                                },
                                shape = DragonShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.PlaylistAdd,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.add_all_manual))
                            }
                        }
                    }
                }
            }
        }
    )
}
