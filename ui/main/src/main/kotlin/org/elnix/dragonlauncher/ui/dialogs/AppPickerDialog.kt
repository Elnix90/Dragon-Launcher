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
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.workspace.AppDrawerSearch
import org.elnix.dragonlauncher.ui.helpers.workspace.AppGrid
import org.elnix.dragonlauncher.ui.helpers.workspace.WorkspaceLockedContent
import org.elnix.dragonlauncher.ui.helpers.workspace.WorkspaceUnavailableContent

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppPickerDialog(
    profilesViewModel: ProfilesViewModel = activityViewModel(),
    drawerViewModel: DrawerViewModel = activityViewModel(),
    multiSelectEnabled: Boolean = false,
    onDismiss: () -> Unit,
    onAppSelected: (Application) -> Unit,
    onMultipleAppsSelected: ((List<Application>) -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    var searchQuery by drawerViewModel.searchQuery
    var isSearchBarEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        drawerViewModel.clearSearchQuery()
    }

    // Auto Show keyboard logic
    LaunchedEffect(isSearchBarEnabled) {
        if (isSearchBarEnabled) {
            yield()
            focusRequester.requestFocus()
        }
    }

    val workspaces by drawerViewModel.activeWorkspaces.collectAsState()
    val selectedWorkspaceId by drawerViewModel.selectedWorkspaceId.collectAsState()

    val initialIndex = workspaces.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (workspaces.size - 1).coerceAtLeast(0)),
        pageCount = { workspaces.size }
    )

    // Multi-select state
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedApps = remember { mutableStateListOf<Application>() }

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
                drawerViewModel.clearSearchQuery()
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
                                    icon = R.drawable.deselect,
                                    contentDescription = R.string.deselect_all,
                                ) {
                                    isMultiSelectMode = false
                                    selectedApps.clear()
                                }
                            }

                            DragonIconButton(
                                icon = R.drawable.search,
                                contentDescription = R.string.search_apps
                            ) { isSearchBarEnabled = true }

                            DragonIconButton(
                                icon = R.drawable.reload,
                                contentDescription = R.string.reload_apps
                            ) { scope.launch { drawerViewModel.reloadApps() } }
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
                                        drawerViewModel.clearSearchQuery()
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
                            shapes = ButtonDefaults.shapes(),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = animatedColor
                            )
                        ) {
                            Text(
                                text = workspace.id,
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

                AnimatedVisibility(isMultiSelectMode && selectedApps.isNotEmpty() && onMultipleAppsSelected != null) {
                    if (onMultipleAppsSelected != null) {
                        DragonButton(
                            onClick = {
                                onMultipleAppsSelected(selectedApps)
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
        },
        text = {
            val profiles by profilesViewModel.profiles.collectAsState(emptyList())
            val profileStates by profilesViewModel.profileStates.collectAsState(emptyList())

            HorizontalPager(pagerState) { pageIndex ->

                val workspace = workspaces[pageIndex]

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

                val apps by drawerViewModel.search(workspace).collectAsStateWithLifecycle()

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
                            isMultiSelectMode = isMultiSelectMode,
                            onEnterMultiSelect = { app ->
                                isMultiSelectMode = true
                                if (app !in selectedApps) {
                                    selectedApps.add(app)
                                }
                            },
                            onToggleSelect = { app ->
                                if (app in selectedApps) {
                                    selectedApps.remove(app)
                                } else {
                                    selectedApps.add(app)
                                }
                                if (selectedApps.isEmpty()) {
                                    isMultiSelectMode = false
                                }
                            },
                            selectedPackages = selectedApps,
                            onReload = drawerViewModel::reloadApps,
                            longPressPopup = false,
                            onClick = {
                                onAppSelected(it)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    )
}
