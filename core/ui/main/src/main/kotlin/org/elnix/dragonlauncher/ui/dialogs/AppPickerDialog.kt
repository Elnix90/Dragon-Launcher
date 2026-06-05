package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Profile.Type.Personal
import org.elnix.dragonlauncher.base.model.serializables.Profile.Type.Private
import org.elnix.dragonlauncher.base.model.serializables.Profile.Type.Work
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.AppDrawerSearch
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.ui.helpers.WorkspaceLockedContent

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppPickerDialog(
    profilesViewModel: ProfilesViewModel = activityViewModel(),
    drawerViewModel: DrawerViewModel = activityViewModel(),
    multiSelectEnabled: Boolean = false,
    onDismiss: () -> Unit,
    onAppSelected: (Application) -> Unit,
    onMultipleAppsSelected: ((List<Application>, Boolean) -> Unit)? = null
) {
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


    val workspaceState by drawerViewModel.workspaceManager.workspacesState.collectAsState()

    val selectedWorkspaceId by drawerViewModel.selectedWorkspaceId.collectAsState()
    val initialIndex = workspaceState.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (workspaceState.size - 1).coerceAtLeast(0)),
        pageCount = { workspaceState.size }
    )

    val scope = rememberCoroutineScope()

    // Multi-select state
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedApps = remember { mutableStateListOf<String>() }

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
                                    colors = AppObjectsColors.iconButtonColors(),
                                    icon = R.drawable.deselect,
                                    contentDescription = stringResource(R.string.deselect_all),
                                ) {
                                    isMultiSelectMode = false
                                    selectedApps.clear()
                                }
                            }

                            DragonIconButton(
                                colors = AppObjectsColors.iconButtonColors(),
                                icon = R.drawable.search,
                                contentDescription = stringResource(R.string.search_apps)
                            ) { isSearchBarEnabled = true }

//                            DragonIconButton(
//                                colors = AppObjectsColors.iconButtonColors(),
//                                icon = R.drawable.reload,
//                                contentDescription = stringResource(R.string.reload_apps)
//                            ) { scope.launch { appsViewModel.reloadApps() } }
                        }
                    } else {
                        AppDrawerSearch(
                            searchQuery = searchQuery,
                            onSearchChanged = { searchQuery = it },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.close),
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

                Spacer(6.dp)

                val listState = rememberLazyListState()

                LaunchedEffect(pagerState.currentPage) {
                    listState.animateScrollToItem(pagerState.currentPage)
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    itemsIndexed(workspaceState) { index, workspace ->
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

                // Multi-select action bar
                AnimatedVisibility(isMultiSelectMode && selectedApps.isNotEmpty() && onMultipleAppsSelected != null) {
                    if (onMultipleAppsSelected != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .settingsGroup(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val allApps by drawerViewModel.allApps.collectAsState()

                            DragonButton(
                                onClick = {
                                    val allApps = allApps
                                    val pickedApps = allApps.filter { it.packageName in selectedApps }
                                    onMultipleAppsSelected(pickedApps, true)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.playlist_add_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(8.dp)
                                Text(stringResource(R.string.add_all_auto))
                            }

                            DragonButton(
                                onClick = {
                                    val pickedApps = allApps.filter { it.packageName in selectedApps }
                                    onMultipleAppsSelected(pickedApps, false)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.playlist_add),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(8.dp)
                                Text(stringResource(R.string.add_all_manual))
                            }
                        }
                    }
                }
            }
        },
        text = {
            val profiles by profilesViewModel.profiles.collectAsState(emptyList())
            val profileStates by profilesViewModel.profileStates.collectAsState(emptyList())

            HorizontalPager(pagerState) { pageIndex ->

                val workspace = workspaceState[pageIndex]

                val workspaceProfileType = when (workspace.type) {
                    WorkspaceType.WORK -> Work
                    WorkspaceType.PRIVATE -> Private
                    else -> Personal
                }

                val workspaceProfile = when (workspaceProfileType) {
                    Personal -> profiles[0]
                    Work -> profiles[1]
                    Private -> profiles[2]
                }

                val workspaceLocked = when (workspaceProfileType) {
                    Work -> profileStates[1]!!.locked
                    Private -> profileStates[2]!!.locked
                    Personal -> false
                }

                val apps by drawerViewModel.search(workspace).collectAsStateWithLifecycle()

                if (workspaceLocked) {
                    WorkspaceLockedContent(workspaceProfile)
                } else {
                    AppGrid(
                        apps = apps,
                        isMultiSelectMode = isMultiSelectMode,
//                        onReload = {
//                            scope.launch {
//                                if (workspace.type == WorkspaceType.PRIVATE) appsViewModel.unlockAndReloadPrivateSpace()
//                                else appsViewModel.reloadApps()
//                            }
//                        },
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
                        longPressPopup = false,
                        onClick = {
                            onAppSelected(it)
                            onDismiss()
                        }
                    )
                }
            }
        }
    )
}
