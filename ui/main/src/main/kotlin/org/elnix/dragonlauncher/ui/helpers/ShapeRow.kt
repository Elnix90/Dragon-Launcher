package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun DragonGroupScope.ShapeRow(
    selected: IconShape,
    title: String = stringResource(R.string.edit_icons_shape),
    resetEnabled: Boolean,
    onReset: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .dragonSettingGroup {
                    clickable(onClick = onClick)
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
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
        ResetIcon(resetEnabled, onReset)
    }
}

@Composable
fun SmallShapeRow(
    selected: IconShape,
    onReset: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
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

        ResetIcon(onReset = onReset)
    }
}
