package org.elnix.dragonlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Points
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.base.util.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.actions.FinalPointIcon
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog

@Composable
fun SelectedPointsTopBar(
    modifier: Modifier,
    points: Points,
    selectedPointsIds: List<Int>,
    onDeselect: (Int) -> Unit,
    onInvert: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    var showSelectedPointsDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPointsIds) {
        if (selectedPointsIds.isEmpty()) showSelectedPointsDialog = false
    }


    var frozenIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var frozenPoints by remember { mutableStateOf(points) }
    if (selectedPointsIds.isNotEmpty()) {
        frozenIds = selectedPointsIds
        frozenPoints = points
    }

    AnimatedVisibility(
        visible = selectedPointsIds.isNotEmpty(),
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = bouncySpec()
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight },
            animationSpec = bouncySpec()
        ) + fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .shapedClickable { showSelectedPointsDialog = true }
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DragonIconButton(
                icon = R.drawable.close,
                contentDescription = R.string.deselect_all,
                onClick = onDeselectAll
            )

            Spacer(5.dp)

            Text(
                pluralStringResource(
                    R.plurals.n_points_selected,
                    frozenIds.size,
                    frozenIds.size
                ),
                modifier = Modifier.padding(3.dp)
            )

            Box {
                DragonIconButton(
                    icon = R.drawable.more_vert,
                    contentDescription = R.string.more,
                    onClick = { showMoreDialog = true }
                )

                BurgerListAction(
                    actions = listOf(
                        MoreOptions(
                            onClick = onSelectAll,
                            icon = R.drawable.select_all,
                            text = { stringResource(R.string.select_all) }
                        ),
                        MoreOptions(
                            onClick = onInvert,
                            icon = R.drawable.swap_calls,
                            text = { stringResource(R.string.invert) }
                        )
                    ),
                    isExpanded = showMoreDialog,
                    onDismissRequest = { showMoreDialog = false }
                )
            }
        }
    }


    if (showSelectedPointsDialog) {
        CustomAlertDialog(
            onDismissRequest = { showSelectedPointsDialog = false },
            modifier = Modifier.padding(36.dp),
            imePadding = false,
            scroll = false,
            alignment = Alignment.Center,
            confirmButton = {
                ValidateCancelButtons(validateText = stringResource(R.string.ok)) { showSelectedPointsDialog = false }
            },
            title = {
                Text(
                    text = stringResource(R.string.selected_points),
                    style = MaterialTheme.typography.titleLargeEmphasized
                )
            }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(60.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.heightIn(min = 200.dp)
            ) {
                items(selectedPointsIds) { pointId ->
                    points.find { it.id == pointId }?.let { point ->
                        PointItem(point) {
                            onDeselect(point.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PointItem(
    point: Point,
    deselect: () -> Unit
) {
    val color = point.action.actionColor(LocalExtraColors.current)
    Row(
        modifier = Modifier
            .shapedClickable(onClick = deselect)
            .background(color.alphaMultiplier(0.2f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FinalPointIcon(point)
        Text(
            text = point.id.toString(),
            color = color,
            style = MaterialTheme.typography.bodyMediumEmphasized
        )
    }
}