@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.CustomGlow
import org.elnix.dragonlauncher.common.serializables.CustomObjectBlockProperties
import org.elnix.dragonlauncher.common.serializables.CustomObjectSerializable
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.ShapeRow

@Composable
fun EditCustomObjectBlock(
    editObject: CustomObjectSerializable,
    default: CustomObjectSerializable,
    properties: CustomObjectBlockProperties = CustomObjectBlockProperties(),
    onEdit: (CustomObjectSerializable) -> Unit
) {

    var tempSize by remember { mutableStateOf(editObject.size) }
    var tempStroke by remember { mutableStateOf(editObject.stroke) }
    var tempRotation by remember { mutableStateOf(editObject.rotation) }
    var tempColor by remember { mutableStateOf(editObject.color) }

    var tempGlowColor by remember { mutableStateOf(editObject.glow?.color) }
    var tempGlowRadius by remember { mutableStateOf(editObject.glow?.radius) }

    var showSelectedShapePickerDialog by remember { mutableStateOf(false) }


    /**
     * Triggers the edit, with all temp values copied, to avoid strange behaviors when editing a value,
     * then another and resting one, only the modified is being edited, the others would be discarded otherwise
     */
    fun triggerEdit() {
        onEdit(
            editObject.copy(
                size = tempSize,
                stroke = tempStroke,
                rotation = tempRotation,
                color = tempColor,
                glow = editObject.glow?.copy(
                    color = tempGlowColor,
                    radius = tempGlowRadius
                )
            )
        )
    }


    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (properties.allowSizeCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.size),
                value = tempSize ?: default.size!!,
                valueRange = 0f..500f,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                decimals = 1,
                onReset = {
                    tempSize = null
                    triggerEdit()
                },
                onDragStateChange = { triggerEdit() }
            ) { tempSize = it }
        }

        if (properties.allowStrokeCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.stroke),
                value = tempStroke ?: default.stroke!!,
                valueRange = 0f..50f,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                decimals = 1,
                onReset = {
                    tempStroke = null
                    triggerEdit()
                },
                onDragStateChange = { triggerEdit() }
            ) { tempStroke = it }
        }

        if (properties.allowRotationCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.rotation),
                description = stringResource(R.string.minus_one_means_random),
                value = tempRotation ?: default.rotation!!,
                valueRange = -1..360, // -1 means random rotation
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onReset = {
                    tempRotation = null
                    triggerEdit()
                },
                onDragStateChange = { triggerEdit() }
            ) { tempRotation = it }
        }

        if (properties.allowColorCustomization) {
            ColorPickerRow(
                label = stringResource(R.string.color),
                showLabel = true,
                enabled = true,
                currentColor = tempColor ?: Color.Unspecified,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onColorPicked = {
                    tempColor = it
                    triggerEdit()
                }
            )
        }

        if (properties.allowGlowCustomization) {
            SwitchRow(
                state = editObject.glow != null,
                title = stringResource(R.string.enable_glow)
            ) { enabled ->
                if (enabled) {
                    onEdit(editObject.copy(glow = CustomGlow()))
                } else {
                    onEdit(editObject.copy(glow = null))
                }
            }

            AnimatedVisibility(editObject.glow != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ColorPickerRow(
                        label = stringResource(R.string.glow_color),
                        showLabel = true,
                        enabled = true,
                        currentColor = tempGlowColor ?: default.glow?.color ?: Color.Unspecified,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        onColorPicked = {
                            tempGlowColor = it
                            triggerEdit()
                        }
                    )


                    SliderWithLabel(
                        label = stringResource(R.string.glow_radius),
                        value = tempGlowRadius ?: default.glow?.radius!!,
                        valueRange = 0f..200f,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        decimals = 1,
                        onReset = {
                            tempGlowRadius = null
                            triggerEdit()
                        },
                        onDragStateChange = {
                            triggerEdit()
                        }
                    ) { tempGlowRadius = it }
                }
            }
        }

        if (properties.allowShapeCustomization) {
            ShapeRow(
                editObject.shape ?: default.shape!!,
                title = stringResource(R.string.edit_shape),
                onReset = { onEdit(editObject.copy(shape = null)) }
            ) { showSelectedShapePickerDialog = true }
        }

        if (properties.allowEraseBackgroundCustomization) {
            SwitchRow(
                state = editObject.eraseBackground
                    ?: default.eraseBackground!!,
                title = stringResource(R.string.erase_background)
            ) {
                onEdit(editObject.copy(eraseBackground = it))
            }
        }
    }

    if (properties.allowShapeCustomization && showSelectedShapePickerDialog) {
        ShapePickerDialog(
            selected = editObject.shape ?: default.shape!!,
            onDismiss = { showSelectedShapePickerDialog = false }
        ) {
            onEdit(editObject.copy(shape = it))
        }
    }
}
