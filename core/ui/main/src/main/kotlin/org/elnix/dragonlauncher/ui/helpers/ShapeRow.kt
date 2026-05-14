package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription


@Composable
fun ShapeRow(
    selected: IconShape,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.edit_icons_shape),
    onReset: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(DragonShape)
            .shapedClickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.weight(1f)
        ) {

            ShapePreview(
                iconShape = selected,
                modifier = Modifier.size(60.dp)
            )

            TextWithDescription(
                text = title,
                description = stringResource(R.string.edit_icons_shape_desc),
                modifier = Modifier.weight(1f)
            )
        }

        DragonIconButton(
            icon = R.drawable.reset,
            colors = AppObjectsColors.iconButtonColors(),
            contentDescription = stringResource(R.string.reset),
            onClick = onReset
        )
    }
}


@Composable
fun SmallShapeRow(
    selected: IconShape,
    onReset: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DragonShape)
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        ShapePreview(
            iconShape = selected,
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = stringResource(R.string.shape),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        DragonIconButton(
            icon = R.drawable.reset,
            contentDescription = stringResource(R.string.reset),
            onClick = onReset
        )
    }
}