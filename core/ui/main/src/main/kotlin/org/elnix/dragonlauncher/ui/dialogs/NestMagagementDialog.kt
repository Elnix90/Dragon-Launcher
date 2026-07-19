package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.NestNameEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon

@Composable
public fun NestManagementDialog(
    pointsViewModel: PointsViewModel = activityViewModel(),
    onDismissRequest: () -> Unit,
    title: String? = null,
    onSelect: ((Nest) -> Unit)? = null
) {
    val pointsService = pointsViewModel.pointsService
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

    CustomAlertDialog(
        modifier = Modifier.padding(15.dp),
        onDismissRequest = onDismissRequest,
        alignment = Alignment.Center,
        scroll = false,
        title = {
            Text(
                text = title ?: stringResource(R.string.manage_nests),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.heightIn(max = 700.dp),
                state = listState
            ) {
                item {
                    DragonButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            hasClickedNewNest = pointsService.addNest()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.add_circle),
                            contentDescription = stringResource(R.string.create_new_nest),
                        )
                        Spacer(15.dp)
                        Text(stringResource(R.string.create_new_nest))
                    }
                }

                items(nestsList) { (_, nest) ->
                    NestManagementItem(
                        nest = nest,
                        modifier = Modifier.animateItem(),
                        onSelect = { onSelect?.invoke(nest) }
                    )
                }
            }
        }
    )
}


@Composable
private fun NestManagementItem(
    pointsViewModel: PointsViewModel = activityViewModel(),
    nest: Nest,
    modifier: Modifier,
    onSelect: (() -> Unit)? = null
) {
    val ctx = LocalContext.current
    val pointsService = pointsViewModel.pointsService

    val bgColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(MaterialTheme.shapes.large)
            .background(bgColor)
            .clickable { onSelect?.invoke() }
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {

        BoxWithConstraints(
            modifier = Modifier
                .weight(0.5f)
                .size(100.dp)
        ) {
            val center = constraints.getCenter()
            PointIcon(
                selected = false,
                point = Point(
                    offset = Offset.Zero,
                    action = Action.OpenCircleNest(nest.id),
                    id = -3
                ),
                alpha = 0.4f,
                center = center,
                preventBgErasing = true,
                eraseColor = bgColor
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .height(IntrinsicSize.Min)
                    .padding(end = 8.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable {
                        ctx.copyToClipboard(nest.id.toString())
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "ID: ${nest.id}",
                    color = MaterialTheme.colorScheme.onSurface.copy(0.9f),
                    fontSize = 10.sp
                )
            }

            NestNameEditor(nest, pointsService)
        }


        val enabled = nest.id != 0

        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = stringResource(if (enabled) R.string.delete_nest else R.string.cannot_delete_nest_0),
            colors = AppObjectsColors.cancelIconButtonColors(),
            enabled = enabled
        ) {
            pointsService.removeNest(nest.id)
        }
    }
}
