package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextField
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
import androidx.compose.ui.geometry.center
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
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.nests.actionsInCircle
import org.elnix.dragonlauncher.ui.remembers.rememberSwipeDefaultParams

@Composable
fun NestManagementDialog(
    pointsViewModel: PointsViewModel = activityViewModel(),
    onDismissRequest: () -> Unit,
    title: String? = null,
    onSelect: ((Nest) -> Unit)? = null
) {
    val nests by pointsViewModel.nests.collectAsState()

    var hasClickedNewNest by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    LaunchedEffect(hasClickedNewNest) {
        if (hasClickedNewNest != null) {
            listState.animateScrollToItem(hasClickedNewNest!!)
            hasClickedNewNest = null
        }
    }

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
                            hasClickedNewNest = pointsViewModel.addNest()
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

                items(nests.toList()) { nest ->
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

    val drawParams by rememberSwipeDefaultParams(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    )

    var tempCustomName by remember { mutableStateOf(nest.name ?: "") }


    val editPoint = Point(
        circleNumber = 0,
        angleDeg = 0.0,
        Action.OpenCircleNest(nest.id),
        id = ""
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(DragonShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onSelect?.invoke() }
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Canvas(
            modifier = Modifier
                .size(100.dp)
        ) {
            val center = size.center

            actionsInCircle(
                selected = false,
                point = editPoint,
                center = center,
                depth = 1,
                drawParams = drawParams,
                preventBgErasing = true
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clip(DragonShape)
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

                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = stringResource(R.string.copy_id),
                    modifier = Modifier.size(10.dp)
                )
            }

            TextField(
                value = tempCustomName,
                onValueChange = {
                    tempCustomName = it

                    pointsViewModel.editNest(nest.id) { nest ->
                        nest.copy(name = it)
                    }
                },
                placeholder = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.edit_rounded),
                            contentDescription = stringResource(R.string.custom_name)
                        )
                        Text(
                            text = stringResource(R.string.custom_name),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = AppObjectsColors.outlinedTextFieldColors(removeBorder = true),
                singleLine = true,
                modifier = Modifier
                    .clip(DragonShape)
                    .weight(1f)
            )
        }



        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = stringResource(R.string.delete_circle_nest),
            colors = AppObjectsColors.cancelIconButtonColors()
        ) {
            pointsViewModel.deleteNest(nest.id)
        }
    }
}
