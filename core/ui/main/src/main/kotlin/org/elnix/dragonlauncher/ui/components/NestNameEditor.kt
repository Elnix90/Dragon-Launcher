package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton

@Composable
public fun NestNameEditor(
    nest: Nest,
    pointsService: PointsService,
    modifier: Modifier = Modifier,
) {
    var tempCustomName by remember { mutableStateOf(nest.name ?: "") }
    TextField(
        value = tempCustomName,
        onValueChange = {
            tempCustomName = it

            pointsService.editNest(nest.id) { nest ->
                nest.copy(name = it.takeIf { it.isNotEmpty() })
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
                Text(stringResource(R.string.custom_name))
            }
        },
        trailingIcon = {
            DragonIconButton(
                icon = R.drawable.reset,
                enabled = tempCustomName.isNotEmpty(),
                contentDescription = R.string.reset
            ) {
                tempCustomName = ""

                pointsService.editNest(nest.id) { nest ->
                    nest.copy(name = null)
                }
            }
        },
        colors = AppObjectsColors.outlinedTextFieldColors(removeBorder = true),
        singleLine = true,
        shape = CircleShape,
        modifier = modifier
    )
}
