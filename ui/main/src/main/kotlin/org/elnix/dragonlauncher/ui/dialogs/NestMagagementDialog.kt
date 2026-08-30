package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.MoreIcon
import org.elnix.dragonlauncher.ui.dragon.settings.TextRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestManagementDialog(
    pointsViewModel: PointsViewModel = activityViewModel(),
    title: String? = null,
    onSelect: ((Nest) -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    val pointsService = pointsViewModel.pointsService
    val navigator = LocalNavigator.current
    val recomposeTrigger by pointsService.recomposeTrigger.asState()
    val nests by pointsService.nests.collectAsState()

    var hasClickedNewNest by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    LaunchedEffect(hasClickedNewNest) {
        if (hasClickedNewNest != null) {
            listState.animateScrollToItem(hasClickedNewNest!!)
            hasClickedNewNest = null
        }
    }

    val nestsList = remember(recomposeTrigger, nests.size) { nests.toList() }

    DragonModalBottomSheet(
        onDismissRequest = onDismissRequest,
        skipPartiallyExpanded = true
    ) {
        DialogTitle(title ?: stringResource(R.string.manage_nests))
        Spacer(10.dp)

        DragonButton(
            onClick = {
                hasClickedNewNest = pointsService.addNest()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.add_circle),
                contentDescription = stringResource(R.string.create_new_nest)
            )
            Spacer(15.dp)
            Text(stringResource(R.string.create_new_nest))
        }

        Spacer(15.dp)

        DragonSettingsGroup {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.heightIn(max = 700.dp),
                state = listState
            ) {
                items(nestsList) { (id, nest) ->
                    NestManagementItem(
                        nest = nest,
                        modifier = Modifier.animateItem(),
                        onEditName = { newName ->
                            pointsService.editNest(nest.id) { old ->
                                old.copy(name = newName)
                            }
                        },
                        onDelete = { pointsService.removeNest(nest.id) },
                        onDuplicate = { pointsService.duplicateNest(nest.id) },
                        onEdit = {
                            pointsViewModel.nestsNavigationService.goToNest(id)
                            navigator.navigate(NavigationRoute.NestEdit)
                        },
                        onSelect = { onSelect?.invoke(nest) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DragonGroupScope.NestManagementItem(
    nest: Nest,
    modifier: Modifier,
    onEditName: (newName: String?) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onSelect: (() -> Unit)? = null
) {
    // Same as in dragonSettingGroup, needed to prevent the nest to erase the bg
    val bgColor = MaterialTheme.colorScheme.surfaceContainerHigh

    var showPopup by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .dragonSettingGroup {
                    height(120.dp)
                        .clickable { onSelect?.invoke() }
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .weight(0.5f)
                    .size(100.dp)
        ) {
            val center = constraints.getCenter()
            PointIcon(
                selected = false,
                point =
                    Point(
                        offset = Offset.Zero,
                        action = Action.OpenNest(nest.id),
                        id = -3
                    ),
                center = center,
                eraseColor = bgColor
            )
        }

        DragonSettingsGroup(modifier = Modifier.weight(1f)) {
            this@NestManagementItem.TextRow(
                currentValue = nest.name,
                defaultValue = null,
                label = null,
                placeHolder = stringResource(R.string.custom_name),
                onFinish = onEditName
            )
        }

        Box {
            MoreIcon { showPopup = true }

            DragonDropDownMenu(
                expanded = showPopup,
                onDismissRequest = { showPopup = false }
            ) {
                DropdownMenuGroup(MenuDefaults.groupShapes()) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.edit_nest))
                        },
                        onClick = {
                            onEdit()
                            showPopup = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.edit_rounded),
                                contentDescription = stringResource(R.string.edit_nest)
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.duplicate))
                        },
                        onClick = {
                            onDuplicate()
                            showPopup = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.copy),
                                contentDescription = stringResource(R.string.duplicate)
                            )
                        }
                    )

                    if (nest.id != 0) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.delete_nest))
                            },
                            onClick = {
                                onDelete()
                                showPopup = false
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.delete_forever),
                                    contentDescription = stringResource(R.string.delete_nest),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
