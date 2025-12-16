package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.utils.models.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppPickerDialog(
    appsViewModel: AppDrawerViewModel,
    workspaceViewModel: WorkspaceViewModel,
    gridSize: Int,
    showIcons: Boolean,
    showLabels: Boolean,
    onDismiss: () -> Unit,
    onAppSelected: (SwipeActionSerializable.LaunchApp) -> Unit
) {
    val workspaceState by workspaceViewModel.enabledState.collectAsState()
    val workspaces = workspaceState.workspaces
    val overrides = workspaceState.appOverrides

    val icons by appsViewModel.icons.collectAsState()

    val selectedWorkspaceId by workspaceViewModel.selectedWorkspaceId.collectAsState()
    val initialIndex = workspaces.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (workspaces.size - 1).coerceAtLeast(0)),
        pageCount = { workspaces.size }
    )

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    var searchQuery by remember { mutableStateOf("") }
    var isSearchBarEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val workspaceId = workspaces.getOrNull(pagerState.currentPage)?.id ?: return@LaunchedEffect
        workspaceViewModel.selectWorkspace(workspaceId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSearchBarEnabled) {
                        Text(
                            text = "Select App",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { isSearchBarEnabled = true },
                            colors = AppObjectsColors.iconButtonColors()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_apps)
                            )
                        }

                        IconButton(
                            onClick = { scope.launch { appsViewModel.reloadApps(ctx) } },
                            colors = AppObjectsColors.iconButtonColors()
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reload apps"
                            )
                        }
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.close),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.clickable {
                                        searchQuery = ""
                                        isSearchBarEnabled = false
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search_apps),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = AppObjectsColors.outlinedTextFieldColors(backgroundColor = MaterialTheme.colorScheme.surface.adjustBrightness(0.7f), removeBorder = true),
                            modifier = Modifier
                                .clip(CircleShape),
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    workspaces.forEachIndexed { index, workspace ->
                        val selected = pagerState.currentPage == index

                        TextButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = AppObjectsColors.buttonColors(
                                if (!selected) MaterialTheme.colorScheme.surface else null
                            ),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Text(
                                text = workspace.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.height(350.dp)
            ) { pageIndex ->

                val workspace = workspaces[pageIndex]

                val apps by appsViewModel
                    .appsForWorkspace(workspace, overrides)
                    .collectAsState(initial = emptyList())

                val filteredApps = if (isSearchBarEnabled)
                    apps.filter { it.name.contains(searchQuery) || it.packageName.contains(searchQuery) }
                else apps

                AppGrid(
                    apps = filteredApps,
                    icons = icons,
                    gridSize = gridSize,
                    txtColor = MaterialTheme.colorScheme.onSurface,
                    showIcons = showIcons,
                    showLabels = showLabels
                ) {
                    onAppSelected(SwipeActionSerializable.LaunchApp(it.packageName))
                    onDismiss()
                }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface
    )
}
