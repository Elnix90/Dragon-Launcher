package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppPickerDialog(
    appsViewModel: AppDrawerViewModel,
    gridSize: Int,
    initialPage: Int,
    onDismiss: () -> Unit,
    onAppSelected: (SwipeActionSerializable.LaunchApp) -> Unit
) {
    val userApps by appsViewModel.userApps.collectAsState()
    val workApps by appsViewModel.workProfileApps.collectAsState()
    val systemApps by appsViewModel.systemApps.collectAsState()
    val allApps by appsViewModel.allApps.collectAsState()

    val icons by appsViewModel.icons.collectAsState()

    val pages = mutableListOf("User", "System", "All")
    if (workApps.isNotEmpty()) {
        pages.add(1, "Work")
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pages.size }
    )

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        scope.launch {
            DrawerSettingsStore.setInitialPage(ctx, pagerState.currentPage)
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
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

                // --- PAGE INDICATOR ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    pages.forEachIndexed { index, label ->
                        val selected = pagerState.currentPage == index


                        TextButton(
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = AppObjectsColors.buttonColors(if (!selected) MaterialTheme.colorScheme.surface else null),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Text(
                                text = label,
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

                val list = when (pageIndex) {
                    0 -> userApps
                    1 -> systemApps
                    else -> allApps
                }

                AppGrid(
                    apps = list,
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
