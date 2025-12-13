package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.workspace.WorkspaceViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppPickerDialog(
    appsViewModel: AppDrawerViewModel,
    workspaceViewModel: WorkspaceViewModel,
    gridSize: Int,
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


    LaunchedEffect(pagerState.currentPage) {
        val workspaceId = workspaces.getOrNull(pagerState.currentPage)?.id ?: return@LaunchedEffect
        workspaceViewModel.selectWorkspace(workspaceId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select App",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { scope.launch { appsViewModel.reloadApps(ctx) } },
                        colors = AppObjectsColors.iconButtonColors()
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reload apps"
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

                AppGrid(
                    apps = apps,
                    icons = icons,
                    gridSize = gridSize,
                    txtColor = MaterialTheme.colorScheme.onSurface,
                    showIcons = true,
                    showLabels = true
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
