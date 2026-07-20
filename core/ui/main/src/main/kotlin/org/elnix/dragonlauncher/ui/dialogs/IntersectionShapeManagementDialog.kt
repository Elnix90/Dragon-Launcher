package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape.Companion.IntersectionShapeDefaults
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.cleanString
import org.elnix.dragonlauncher.ktx.getNextId
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.dragonSettingGroupPaddingValues
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.selfAlignHorizontally
import org.elnix.dragonlauncher.ui.components.IntersectionShapePreview
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.EditValueTextField
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock

@Composable
public fun IntersectionShapeManagementDialog(
    shapes: Set<IntersectionShape>,
    modifier: Modifier = Modifier,
    onSave: (newShapes: Set<IntersectionShape>) -> Unit,
    onDismiss: () -> Unit
) {

    val shapesInternal: SnapshotStateSet<IntersectionShape> = remember(shapes) {
        mutableStateSetOf<IntersectionShape>().apply {
            addAll(shapes)
        }
    }

    fun updateShape(id: Int, newShape: (IntersectionShape) -> IntersectionShape) {
        val oldShape = shapesInternal.find { it.id == id } ?: return
        shapesInternal -= oldShape
        shapesInternal += newShape(oldShape)
    }

    var showDetails by remember { mutableStateOf<Int?>(null) }


    CustomAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.heightIn(max = 600.dp),
        imePadding = false,
        scroll = false,
        alignment = Alignment.Center,
        confirmButton = {
            ValidateCancelButtons(onCancel = onDismiss) {
                onSave(shapesInternal)
                onDismiss()
            }
        },
        dismissButton = null,
        icon = null,
        title = {
            DialogTitle(stringResource(R.string.shapes_management))
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(shapesInternal.toList()) { shape ->
                ShapeItem(
                    shape = shape,
                    onChangeShape = { newShape ->
                        updateShape(shape.id) { old ->
                            old.copy(shape = newShape)
                        }
                    },
                    onClone = {
                        shapesInternal += shape.copy(id = shapesInternal.mapTo(mutableSetOf()) { it.id }.getNextId())
                    },
                    onDelete = {
                        shapesInternal.removeIf { it.id == shape.id }
                    },
                    oClick = { showDetails = shape.id }
                )
            }

            item {
                DragonButton(
                    onClick = {
                        val newId = shapesInternal.mapTo(mutableSetOf()) { it.id }.getNextId()

                        shapesInternal.add(
                            IntersectionShape(
                                id = newId,
                                shape = IconShape.Circle,
                                scale = 1.5f,
                                offset = Offset.Zero
                            )
                        )
                    },
                    modifier = modifier.selfAlignHorizontally()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = null
                    )
                    Spacer(5.dp)
                    Text(stringResource(R.string.add_shape))
                }
            }
        }
    }

    if (showDetails != null) {
        val shapeId = showDetails!!
        val shape = shapesInternal.find { it.id == shapeId } ?: IntersectionShape(shapeId)

        ShapeDetailDialog(
            shape,
            onChangeShape = { new ->
                updateShape(shapeId) { _ -> new }
            }) { showDetails = null }
    }
}

@Composable
private fun ShapeItem(
    shape: IntersectionShape,
    onChangeShape: (newShape: IconShape) -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    oClick: () -> Unit
) {
    var showShapeDialog by remember { mutableStateOf(false) }

    DragonRow(
        onClick = oClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        IntersectionShapePreview(
            shape = shape,
            size = 50.dp
        ) { showShapeDialog = true }

        Spacer(12.dp)

        Text("Id: ${shape.id}")
        Spacer()

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "${stringResource(R.string.offset)}: ${shape.offset.cleanString()}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = stringResource(R.string.shape_scale, shape.scale),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = stringResource(R.string.rotation_arg, shape.angle),
                style = MaterialTheme.typography.labelSmall
            )
        }

        DragonIconButton(
            icon = R.drawable.copy,
            contentDescription = R.string.copy,
            onClick = onClone
        )

        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = R.string.remove,
            colors = AppObjectsColors.cancelIconButtonColors(),
            onClick = onDelete
        )
    }

    if (showShapeDialog) {
        ShapePickerDialog(
            selected = shape.shape,
            allowedShapes = IconShape.allowedNestShapes,
            onDismiss = { showShapeDialog = false }
        ) {
            onChangeShape(it)
            showShapeDialog = false
        }
    }
}


@Composable
private fun ShapeDetailDialog(
    shape: IntersectionShape,
    onChangeShape: (newShape: IntersectionShape) -> Unit,
    onDismiss: () -> Unit
) {
    val extraColors = LocalExtraColors.current

    var showHapticFeedbackEditor by remember { mutableStateOf(false) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(24.dp),
        imePadding = true,
        scroll = false,
        alignment = Alignment.Center,
        confirmButton = {
            ValidateCancelButtons(validateText = stringResource(R.string.ok), onConfirm = onDismiss)
        },
        dismissButton = null,
        icon = null,
        title = {
            DialogTitle(stringResource(R.string.edit_shape))
        }
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DragonSettingsGroup(
                title = null,
                contentPadding = dragonSettingGroupPaddingValues
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.offset),
                        style = MaterialTheme.typography.labelMediumEmphasized
                    )

                    Spacer()

                    ShapeOffsetTextField(
                        value = shape.offset.x,
                        modifier = Modifier.weight(1f)
                    ) {
                        onChangeShape(
                            shape.copy(
                                offset = shape.offset.copy(x = it)
                            )
                        )
                    }

                    ShapeOffsetTextField(
                        value = shape.offset.y,
                        modifier = Modifier.weight(1f)
                    ) {
                        onChangeShape(
                            shape.copy(
                                offset = shape.offset.copy(y = it)
                            )
                        )
                    }
                }

                SliderWithLabel(
                    label = stringResource(R.string.scale),
                    value = shape.scale,
                    valueRange = 0f..10f,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    onReset = {
                        onChangeShape(
                            shape.copy(
                                scale = IntersectionShapeDefaults.defaultScale
                            )
                        )
                    }
                ) {
                    onChangeShape(
                        shape.copy(
                            scale = it
                        )
                    )
                }
            }


            // CustomObject Block

            val dummyCustomObject = CustomObject(
                stroke = shape.borderStroke ?: IntersectionShapeDefaults.borderStrokeDefault,
                color = shape.color ?: extraColors.shapes,
                glow = shape.glow,
                shape = shape.shape,
                size = 0f, // Not used
                rotation = shape.angle.toInt(),
                eraseBackground = true,// Not used
                alignsWithDragAngle = false // Not used
            )


            val defaultCustomObject = CustomObject(
                stroke = IntersectionShapeDefaults.borderStrokeDefault,
                color = extraColors.shapes,
                glow = IntersectionShapeDefaults.defaultGlow,
                shape = IntersectionShapeDefaults.defaultShape,
                size = 0f, // Not used
                rotation = shape.angle.toInt(),
                eraseBackground = true, // Not used
                alignsWithDragAngle = false // Not used
            )

            EditCustomObjectBlock(
                title = R.string.custom_object,
                editObject = dummyCustomObject,
                properties = CustomObjectBlockProperties(
                    allowSizeCustomization = false,
                    allowMirrorCustomization = false,
                    allowAlignCustomization = false,
                    allowEraseBackgroundCustomization = false
                ),
                default = defaultCustomObject
            ) { newObject ->
                onChangeShape(
                    shape.copy(
                        shape = newObject.shape,
                        angle = newObject.rotation.toFloat(),
                        borderStroke = newObject.stroke,
                        color = newObject.color,
                        glow = newObject.glow,
                    )
                )
            }

            HapticFeedBackEditorButtonWithPlayTest(shape.haptic ?: IntersectionShapeDefaults.defaultHapticFeedback) {
                showHapticFeedbackEditor = true
            }
        }
    }

    if (showHapticFeedbackEditor) {
        HapticFeedbackEditor(
            initial = shape.haptic,
            onDismiss = {
                showHapticFeedbackEditor = false
            }
        ) {
            onChangeShape(shape.copy(haptic = it))
        }
    }
}

@Composable
private fun ShapeOffsetTextField(
    value: Float,
    modifier: Modifier,
    onDone: (newValue: Float) -> Unit,
) {
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current

    var isError by remember { mutableStateOf(false) }
    var editingText by remember { mutableStateOf(value.toString()) }

    EditValueTextField(
        value = editingText,
        onValueChange = {
            editingText = it
            isError = false
        },
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
        isError = isError,
        onReset = {
            editingText = 0f.toString()
            onDone(0f)
        }
    ) {
        try {
            val clampedValue = editingText.takeIf { it.isNotEmpty() }?.toFloat()?.coerceAtLeast(0f) ?: 0f

            onDone(clampedValue)
        } catch (_: Exception) {
            ctx.showToast("Failed to parse number")
            isError = true
        }
        focusManager.clearFocus()

    }
}