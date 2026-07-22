package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape.Companion.isNotDefault
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedBackEditorButtonWithPlayTest
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedbackEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.EditValueTextField
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun IntersectionShapeEditor(
    shape: IntersectionShape,
    isDefaultEditing: Boolean,
    defaultShape: IntersectionShape,
    onChangeShape: (newShape: IntersectionShape) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val extraColors = LocalExtraColors.current

    val offset = shape.getOffset(defaultShape, isDefaultEditing)
    val scale = shape.getScale(defaultShape, isDefaultEditing)
    val iconShape = shape.getShape(defaultShape, isDefaultEditing)
    val rotation = shape.getRotation(defaultShape, isDefaultEditing)
    val stroke = shape.getBorderStroke(defaultShape, isDefaultEditing)
    val pointsKeepTheirRelativePosition = shape.getPointsKeepTheirRelativePosition(defaultShape, isDefaultEditing)

    var showHapticFeedbackEditor by remember { mutableStateOf(false) }

    DragonModalBottomSheet(onDismiss) {
        DialogTitle(
            text = stringResource(if (!isDefaultEditing) R.string.edit_shape else R.string.edit_default_shape),
            resetEnabled = shape.isNotDefault,
            onReset = onReset
        )

        Column(
            modifier = Modifier
                .heightIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DragonSettingsGroup(R.string.position) {
                val offsetRange = -2000f..2000f


                SliderWithLabel(
                    label = "X",
                    value = offset.x,
                    valueRange = offsetRange,
                    resetEnabled = shape.offset?.x?.let { x -> x != (defaultShape.offset?.x ?: IntersectionShape.defaultOffset.x) } ?: false,
                    onReset = {
                        onChangeShape(
                            shape.copy(
                                offset = shape.offset?.copy(x = (defaultShape.offset?.x ?: IntersectionShape.defaultOffset.x))
                            )
                        )
                    }
                ) {
                    onChangeShape(
                        shape.copy(
                            offset = shape.offset?.copy(x = it) ?: Offset(x = it, y = (defaultShape.offset?.y ?: IntersectionShape.defaultOffset.y))
                        )
                    )
                }

                SliderWithLabel(
                    label = "Y",
                    value = offset.y,
                    valueRange = offsetRange,
                    resetEnabled = shape.offset?.y?.let { y -> y != (defaultShape.offset?.y ?: IntersectionShape.defaultOffset.y) } ?: false,
                    onReset = {
                        onChangeShape(
                            shape.copy(
                                offset = shape.offset?.copy(y = (defaultShape.offset?.y ?: IntersectionShape.defaultOffset.y))
                            )
                        )
                    }
                ) {
                    onChangeShape(
                        shape.copy(
                            offset = shape.offset?.copy(y = it) ?: Offset(x = (defaultShape.offset?.x ?: IntersectionShape.defaultOffset.x), y = it)
                        )
                    )
                }

                SliderWithLabel(
                    label = stringResource(R.string.scale),
                    value = scale,
                    valueRange = 0f..10f,
                    resetEnabled = shape.scale != null,
                    onReset = {
                        onChangeShape(shape.copy(scale = null))
                    }
                ) {
                    onChangeShape(
                        shape.copy(
                            scale = it
                        )
                    )
                }

                SliderWithLabel(
                    label = stringResource(R.string.rotation),
                    value = rotation,
                    valueRange = 0..360,
                    resetEnabled = shape.rotation != null,
                    onReset = {
                        onChangeShape(shape.copy(rotation = null))
                    }
                ) {
                    onChangeShape(shape.copy(rotation = it))
                }
            }


            val dummyCustomObject = CustomObject(
                stroke = stroke,
                color = shape.color ?: extraColors.shapes,
                glow = shape.glow,
                shape = iconShape,
                size = Dp.Unspecified, // Not used
                rotation = 0, // Not used
                eraseBackground = true,// Not used
                alignsWithDragAngle = false // Not used
            )

            val defaultCustomObject = CustomObject(
                stroke = IntersectionShape.defaultBorderStroke,
                color = extraColors.shapes,
                glow = IntersectionShape.defaultGlow,
                shape = IntersectionShape.defaultShape,
                size = Dp.Unspecified, // Not used
                rotation = 0, // Not used
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
                    allowEraseBackgroundCustomization = false,
                    allowRotationCustomization = false,
                    allowedShapes = IconShape.allowedNestShapes
                ),
                default = defaultCustomObject
            ) { newObject ->
                onChangeShape(
                    shape.copy(
                        shape = newObject.shape,
                        borderStroke = newObject.stroke,
                        color = newObject.color,
                        glow = newObject.glow,
                    )
                )
            }

            HapticFeedBackEditorButtonWithPlayTest(shape.haptic ?: IntersectionShape.defaultHapticFeedback) {
                showHapticFeedbackEditor = true
            }

            DragonSettingsGroup(R.string.advanced) {
                SwitchRow(
                    state = pointsKeepTheirRelativePosition,
                    title = stringResource(R.string.points_keep_their_relative_position),
                    description = stringResource(R.string.points_keep_their_relative_position_desc),
                    resetEnabled = shape.pointsKeepTheirRelativePosition != null,
                    onReset = {
                        onChangeShape(shape.copy(pointsKeepTheirRelativePosition = null))
                    }
                ) {
                    onChangeShape(shape.copy(pointsKeepTheirRelativePosition = it))
                }
            }
        }
    }

    if (showHapticFeedbackEditor) {
        HapticFeedbackEditor(
            initial = shape.haptic,
            onReset = { onChangeShape(shape.copy(haptic = null)) }
        ) {
            onChangeShape(shape.copy(haptic = it))
            showHapticFeedbackEditor = false
        }
    }
}

@Composable
private fun ShapeOffsetTextField(
    title: String,
    value: Float,
    modifier: Modifier,
    onDone: (newValue: Float) -> Unit,
) {
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current

    var isError by remember { mutableStateOf(false) }
    var editingText by remember { mutableStateOf(value.toString()) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier,
    ) {
        Text(
            text = "$title =",
            style = MaterialTheme.typography.labelLargeEmphasized
        )

        EditValueTextField(
            value = editingText,
            onValueChange = {
                editingText = it
                isError = false
            },
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            isError = isError,
            resetEnabled = value != 0f,
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
}