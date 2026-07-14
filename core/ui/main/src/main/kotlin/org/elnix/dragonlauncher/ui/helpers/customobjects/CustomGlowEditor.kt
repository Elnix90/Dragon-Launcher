package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow

@Composable
public fun ColumnScope.CustomGlowEditor(
    editGlow: CustomObject,
    onEdit: (CustomObject) -> Unit,
    default: CustomObject
) {
    SwitchRow(
        state = editGlow.glow != null,
        title = stringResource(R.string.enable_glow)
    ) { enabled ->
        if (enabled) {
            onEdit(editGlow.copy(glow = default.glow))
        } else {
            onEdit(editGlow.copy(glow = null))
        }
    }

    AnimatedVisibility(editGlow.glow != null) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            ColorPickerRow(
                title = stringResource(R.string.glow_color),
                description = null,
                enabled = true,
                currentColor = editGlow.glow?.color ?: default.glow?.color ?: Color.Unspecified,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onColorPicked = { newColor ->
                    onEdit(
                        editGlow.copy(
                            glow = editGlow.glow
                                ?.copy(color = newColor)
                                ?: CustomGlow(radius = default.glow!!.radius, color = newColor)
                        )
                    )
                }
            )

            SliderWithLabel(
                label = stringResource(R.string.glow_radius),
                value = editGlow.glow?.radius ?: default.glow?.radius!!,
                valueRange = 0f..200f,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                decimals = 1,
                onReset = {
                    onEdit(editGlow.copy(glow = editGlow.glow?.copy(radius = default.glow!!.radius) ?: CustomGlow(default.glow!!.radius)))
                }
            ) {
                onEdit(editGlow.copy(glow = editGlow.glow?.copy(radius = it) ?: CustomGlow(radius = it)))
            }
        }
    }
}