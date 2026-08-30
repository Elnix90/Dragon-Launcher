package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape.Companion.emptyIntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape.Companion.isNotDefault
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ktx.round
import org.elnix.dragonlauncher.ktx.snapToGrid
import org.elnix.dragonlauncher.ktx.snapToRound
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedBackEditorButtonWithPlayTest
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedbackEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IntersectionShapeEditor(
    shape: IntersectionShape,
    isDefaultEditing: Boolean,
    defaultShape: IntersectionShape,
    onChangeShape: (newShape: IntersectionShape) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val extraColors = LocalExtraColors.current
    val nestDebugInfo by DebugSettingsStore.nestDebugInfo.asState()

    val snapShapesOffset by UiSettingsStore.snapShapesOffset.asState()
    val snapShapesCenter by UiSettingsStore.snapShapesCenter.asState()
    val snapShapeAngle by UiSettingsStore.snapShapeAngle.asState()
    val snapOffsetThreshold = 30.dp.px

    val cellSizeDp by UiSettingsStore.nestsCellSizeDp.asState()
    val cellSizePx = cellSizeDp.px

    val x = shape.getOffsetX(defaultShape, isDefaultEditing)
    val y = shape.getOffsetY(defaultShape, isDefaultEditing)
    val scale = shape.getScale(defaultShape, isDefaultEditing)
    val iconShape = shape.getShape(defaultShape, isDefaultEditing)
    val rotation = shape.getRotation(defaultShape, isDefaultEditing)
    val stroke = shape.getBorderStroke(defaultShape, isDefaultEditing)
    val color = shape.getColor(defaultShape, extraColors, isDefaultEditing)
    val haptic = shape.getHapticFeedback(defaultShape, isDefaultEditing)
//    val pointsKeepTheirRelativePosition = shape.getPointsKeepTheirRelativePosition(defaultShape, isDefaultEditing)

    var showHapticFeedbackEditor by remember { mutableStateOf(false) }

    DragonModalBottomSheet(onDismiss) {
        DialogTitle(
            text = stringResource(if (!isDefaultEditing) R.string.edit_shape else R.string.edit_default_shape),
            resetEnabled = shape.isNotDefault,
            onReset = onReset
        )

        if (nestDebugInfo) {
            Text(
                text = shape.toString(),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontFamily = FontFamily.Monospace,
                modifier =
                    Modifier
                        .padding(10.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(5.dp)
            )
        }

        Column(
            modifier =
                Modifier
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
        ) {
            DragonSettingsGroup(R.string.position) {
                if (!isDefaultEditing) { // Too annoying to handle movement when moving the default shape so you can't
                    val offsetRange = -2000f..2000f
                    val defaultOffset = emptyIntersectionShape.getOffset(defaultShape, false)
                    SliderWithLabel(
                        label = "X",
                        value = x,
                        valueRange = offsetRange,
                        resetEnabled = shape.offset?.x?.let { x -> x != defaultOffset.x } ?: false,
                        onReset = {
                            onChangeShape(
                                shape.copy(
                                    offset = shape.offset?.copy(x = defaultOffset.x)
                                )
                            )
                        }
                    ) { newValue ->

                        val offset = shape.offset?.copy(x = newValue) ?: Offset(x = newValue, y = defaultOffset.y)
                        val newSnappedOffset =
                            when {
                                snapShapesCenter && snapShapesOffset -> offset.snapToGrid(cellSizePx).snapToRound(Offset.Zero, snapOffsetThreshold)
                                snapShapesCenter -> offset.snapToRound(Offset.Zero, snapOffsetThreshold)
                                snapShapesOffset -> offset.snapToGrid(cellSizePx)
                                else -> offset
                            }

                        val finalNew = newSnappedOffset.takeIf { it.x.round(2) != defaultOffset.x }
                        if (finalNew == shape.offset) return@SliderWithLabel
                        onChangeShape(shape.copy(offset = finalNew))
                    }

                    SliderWithLabel(
                        label = "Y",
                        value = y,
                        valueRange = offsetRange,
                        resetEnabled = shape.offset?.y?.let { y -> y != defaultOffset.y } ?: false,
                        onReset = {
                            onChangeShape(
                                shape.copy(
                                    offset = shape.offset?.copy(y = defaultOffset.y)
                                )
                            )
                        }
                    ) { newValue ->

                        val offset = shape.offset?.copy(y = newValue) ?: Offset(x = defaultOffset.x, y = newValue)
                        val newSnappedOffset =
                            when {
                                snapShapesCenter && snapShapesOffset -> offset.snapToGrid(cellSizePx).snapToRound(Offset.Zero, snapOffsetThreshold)
                                snapShapesCenter -> offset.snapToRound(Offset.Zero, snapOffsetThreshold)
                                snapShapesOffset -> offset.snapToGrid(cellSizePx)
                                else -> offset
                            }

                        val finalNew = newSnappedOffset.takeIf { it.y.round(2) != defaultOffset.y }
                        if (finalNew == shape.offset) return@SliderWithLabel
                        onChangeShape(shape.copy(offset = finalNew))
                    }
                }

                SliderWithLabel(
                    label = stringResource(R.string.scale),
                    value = scale,
                    valueRange = 0f..10f,
                    resetEnabled = shape.scale != null,
                    onReset = {
                        onChangeShape(shape.copy(scale = null))
                    }
                ) { newValue ->
                    onChangeShape(
                        shape.copy(
                            scale = newValue.takeIf { it.round(2) != emptyIntersectionShape.getScale(defaultShape, isDefaultEditing) }
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
                ) { newValue ->
                    val newRotation = if (snapShapeAngle) newValue.snapToRound(0, 20) else newValue
                    val finalNew = newRotation.takeIf { it != emptyIntersectionShape.getRotation(defaultShape, isDefaultEditing) }
                    if (finalNew == shape.rotation) return@SliderWithLabel
                    onChangeShape(shape.copy(rotation = finalNew))
                }
            }

            val dummyCustomObject =
                CustomObject(
                    stroke = stroke,
                    color = color,
                    glow = shape.glow,
                    shape = iconShape,
                    size = Dp.Unspecified, // Not used
                    rotation = 0, // Not used
                    eraseBackground = true, // Not used
                    alignsWithDragAngle = false // Not used
                )

            val defaultCustomObject =
                CustomObject(
                    stroke = emptyIntersectionShape.getBorderStroke(defaultShape, isDefaultEditing),
                    color = emptyIntersectionShape.getColor(defaultShape, extraColors, isDefaultEditing),
                    glow = emptyIntersectionShape.getGlow(defaultShape, isDefaultEditing),
                    shape = emptyIntersectionShape.getShape(defaultShape, isDefaultEditing),
                    size = Dp.Unspecified, // Not used
                    rotation = 0, // Not used
                    eraseBackground = true, // Not used
                    alignsWithDragAngle = false // Not used
                )

            EditCustomObjectBlock(
                title = R.string.custom_object,
                editObject = dummyCustomObject,
                properties =
                    CustomObjectBlockProperties(
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
                        shape = newObject.shape.takeIf { it != emptyIntersectionShape.getShape(defaultShape, isDefaultEditing) },
                        borderStroke =
                            newObject.stroke.takeIf {
                                it.value.round(2) !=
                                    emptyIntersectionShape
                                        .getBorderStroke(
                                            defaultShape,
                                            isDefaultEditing
                                        ).value
                                        .round(2)
                            },
                        color = newObject.color.takeIf { it != emptyIntersectionShape.getColor(defaultShape, extraColors, isDefaultEditing) },
                        glow = newObject.glow.takeIf { it != emptyIntersectionShape.getGlow(defaultShape, isDefaultEditing) }
                    )
                )
            }

            DragonSettingsGroup(R.string.haptic_feedback) {
                HapticFeedBackEditorButtonWithPlayTest(haptic) {
                    showHapticFeedbackEditor = true
                }
            }

            // Not implemented for now TODO
//            DragonSettingsGroup(R.string.advanced) {
//                SwitchRow(
//                    state = pointsKeepTheirRelativePosition,
//                    title = R.string.points_keep_their_relative_position,
//                    description = R.string.points_keep_their_relative_position_desc,
//                    resetEnabled = shape.pointsKeepTheirRelativePosition != null,
//                    onReset = {
//                        onChangeShape(shape.copy(pointsKeepTheirRelativePosition = null))
//                    }
//                ) {
//                    onChangeShape(
//                        shape.copy(
//                            pointsKeepTheirRelativePosition = it.takeIf {
//                                it != emptyIntersectionShape.getPointsKeepTheirRelativePosition(
//                                    defaultShape,
//                                    isDefaultEditing
//                                )
//                            }
//                        )
//                    )
//                }
//            }
        }
    }

    val defaultHaptic = emptyIntersectionShape.getHapticFeedback(defaultShape, isDefaultEditing)
    if (showHapticFeedbackEditor) {
        HapticFeedbackEditor(
            initial = shape.haptic,
            default = defaultHaptic
        ) { newCustomHaptic ->
            onChangeShape(
                shape.copy(
                    haptic = newCustomHaptic.takeIf { it != defaultHaptic }
                )
            )
            showHapticFeedbackEditor = false
        }
    }
}
