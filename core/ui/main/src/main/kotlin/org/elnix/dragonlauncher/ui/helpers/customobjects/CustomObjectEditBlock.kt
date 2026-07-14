package org.elnix.dragonlauncher.ui.helpers.customobjects

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
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObjectBlockProperties
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.ShapeRow

@Composable
public fun EditCustomObjectBlock(
    title: Int? = null, // TODO
    editObject: CustomObject,
    default: CustomObject,
    properties: CustomObjectBlockProperties = CustomObjectBlockProperties(),
    onEdit: (CustomObject) -> Unit
) {

    var showSelectedShapePickerDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (properties.allowSizeCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.size),
                value = editObject.size,
                valueRange = 0f..500f,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                decimals = 1,
                onReset = { onEdit(editObject.copy(size = default.size)) },
                onChange = { onEdit(editObject.copy(size = it)) }
            )
        }

        if (properties.allowStrokeCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.stroke),
                value = editObject.stroke,
                valueRange = 0f..200f,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                decimals = 1,
                onReset = { onEdit(editObject.copy(stroke = default.stroke)) },
                onChange = { onEdit(editObject.copy(stroke = it)) }
            )
        }

        if (properties.allowRotationCustomization) {
            SliderWithLabel(
                label = stringResource(R.string.rotation),
                description = stringResource(R.string.minus_one_means_random),
                value = editObject.rotation,
                valueRange = -1..360, // -1 means random rotation
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onReset = { onEdit(editObject.copy(rotation = default.rotation)) },
                onChange = { onEdit(editObject.copy(rotation = it)) }
            )
        }

        if (properties.allowColorCustomization) {
            ColorPickerRow(
                title = stringResource(R.string.color),
                description = null,
                enabled = true,
                currentColor = editObject.color ?: Color.Unspecified,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onColorPicked = { onEdit(editObject.copy(color = it)) }
            )
        }

        if (properties.allowGlowCustomization) {
            CustomGlowEditor(editObject, onEdit, default)
        }

        if (properties.allowShapeCustomization) {
            ShapeRow(
                selected = editObject.shape,
                title = stringResource(R.string.edit_shape),
                onReset = { onEdit(editObject.copy(shape = default.shape)) }
            ) { showSelectedShapePickerDialog = true }
        }
        if (properties.allowMirrorCustomization) {
            SwitchRow(
                state = editObject.mirror,
                title = stringResource(R.string.mirror),
                description = stringResource(R.string.mirror_desc),
            ) {
                onEdit(editObject.copy(mirror = it))
            }
        }
        if (properties.allowEraseBackgroundCustomization) {
            SwitchRow(
                state = editObject.eraseBackground,
                title = stringResource(R.string.erase_background)
            ) {
                onEdit(editObject.copy(eraseBackground = it))
            }
        }
    }

    if (properties.allowShapeCustomization && showSelectedShapePickerDialog) {
        ShapePickerDialog(
            selected = editObject.shape,
            onDismiss = { showSelectedShapePickerDialog = false }
        ) {
            onEdit(editObject.copy(shape = it))
            showSelectedShapePickerDialog = false
        }
    }
}
