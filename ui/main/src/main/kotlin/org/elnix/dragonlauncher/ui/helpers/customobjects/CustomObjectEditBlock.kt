package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.model.serializables.isSpecified
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.ShapeRow

@Composable
fun EditCustomObjectBlock(
    title: Int? = null,
    editObject: CustomObject,
    default: CustomObject,
    properties: CustomObjectBlockProperties = CustomObjectBlockProperties(),
    onEdit: (CustomObject) -> Unit
) {
    var showSelectedShapePickerDialog by remember { mutableStateOf(false) }

    DragonSettingsGroup(title) {
        if (properties.allowSizeCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.size),
                value = editObject.size,
                valueRange = 0.dp..500.dp,
                decimals = 1,
                resetEnabled = editObject.size != default.size,
                onReset = { onEdit(editObject.copy(size = default.size)) },
                onChange = { onEdit(editObject.copy(size = it)) }
            )
        }

        if (properties.allowStrokeCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.stroke),
                description = stringResource(R.string.stroke_width_explanation),
                value = editObject.stroke,
                valueRange = (-1).dp..200.dp,
                decimals = 1,
                resetEnabled = editObject.stroke != default.stroke,
                onReset = { onEdit(editObject.copy(stroke = default.stroke)) },
                onChange = { onEdit(editObject.copy(stroke = it)) }
            )
        }

        if (properties.allowRotationCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.rotation),
                description = stringResource(R.string.minus_one_means_random),
                value = editObject.rotation,
                valueRange = -1..360,
                resetEnabled = editObject.rotation != default.rotation,
                onReset = { onEdit(editObject.copy(rotation = default.rotation)) },
                onChange = { onEdit(editObject.copy(rotation = it)) }
            )
        }

        if (properties.allowColorCustomization) {
            ColorPickerRow(
                title = stringResource(R.string.color),
                description = null,
                enabled = true,
                currentColor = editObject.color ?: default.color,
                defaultColor = null
            ) {
                onEdit(editObject.copy(color = it))
            }
        }

        if (properties.allowGlowCustomization) {
            ColorPickerRow(
                title = stringResource(R.string.glow_color),
                description = null,
                enabled = true,
                currentColor = editObject.glow?.color ?: default.glow?.color,
                defaultColor = null
            ) { newColor ->
                onEdit(
                    editObject.copy(
                        glow =
                            (
                                editObject.glow
                                    ?.copy(color = newColor)
                                    ?: CustomGlow(color = newColor)
                            ).takeIf { it.isSpecified }
                    )
                )
            }

            SliderWithLabel(
                label = stringResource(R.string.glow_radius),
                description = stringResource(R.string.zero_means_no_glow),
                value = editObject.glow?.radius ?: default.glow?.radius!!, // I have no idea how this hasn't crashed yet for me lol
                valueRange = 0.dp..200.dp,
                decimals = 1,
                resetEnabled = editObject.glow?.radius != null,
                onReset = {
                    onEdit(editObject.copy(glow = editObject.glow?.copy(radius = null).takeIf { it.isSpecified }))
                }
            ) { newGlowRadius ->
                onEdit(
                    editObject.copy(
                        glow =
                            (
                                editObject.glow
                                    ?.copy(radius = newGlowRadius)
                                    ?: CustomGlow(radius = newGlowRadius)
                            ).takeIf { it.isSpecified }
                    )
                )
            }
        }

        if (properties.allowShapeCustomization) {
            ShapeRow(
                selected = editObject.shape,
                title = stringResource(R.string.edit_shape),
                resetEnabled = editObject.shape != default.shape,
                onReset = { onEdit(editObject.copy(shape = default.shape)) }
            ) { showSelectedShapePickerDialog = true }
        }
        if (properties.allowMirrorCustomization) {
            SwitchRow(
                state = editObject.mirror,
                title = R.string.mirror,
                description = R.string.mirror_desc,
                resetEnabled = editObject.mirror != default.mirror,
                onReset = {
                    onEdit(editObject.copy(mirror = default.mirror))
                }
            ) {
                onEdit(editObject.copy(mirror = it))
            }
        }
        if (properties.allowAlignCustomization) {
            SwitchRow(
                state = editObject.alignsWithDragAngle,
                title = R.string.align_with_angle,
                description = R.string.align_with_angle_desc,
                resetEnabled = editObject.alignsWithDragAngle != default.alignsWithDragAngle,
                onReset = {
                    onEdit(editObject.copy(alignsWithDragAngle = default.alignsWithDragAngle))
                }
            ) {
                onEdit(editObject.copy(alignsWithDragAngle = it))
            }
        }
        if (properties.allowEraseBackgroundCustomization) {
            SwitchRow(
                state = editObject.eraseBackground,
                title = R.string.erase_background,
                resetEnabled = editObject.eraseBackground != default.eraseBackground,
                onReset = {
                    onEdit(editObject.copy(eraseBackground = default.eraseBackground))
                }
            ) {
                onEdit(editObject.copy(eraseBackground = it))
            }
        }
    }

    if (properties.allowShapeCustomization && showSelectedShapePickerDialog) {
        ShapePickerDialog(
            selected = editObject.shape,
            onDismiss = { showSelectedShapePickerDialog = false },
            allowedShapes = properties.allowedShapes
        ) {
            onEdit(editObject.copy(shape = it))
            showSelectedShapePickerDialog = false
        }
    }
}
