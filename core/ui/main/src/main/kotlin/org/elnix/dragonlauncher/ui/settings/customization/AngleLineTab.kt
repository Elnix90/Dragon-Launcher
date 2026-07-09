package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultEndCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultLineCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultStartCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.angle360FromOffset
import org.elnix.dragonlauncher.ktx.distanceSquaredTo
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.AngleLineObjectsOrderDialog
import org.elnix.dragonlauncher.ui.dialogs.rememberLineObjectsOrder
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsColorPicker
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.actionLine
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson.rememberAngleLineObjects
import org.elnix.dragonlauncher.ui.remembers.rememberSweepAngle

@Composable
public fun AngleLineTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val extraColors = LocalExtraColors.current
    val scope = rememberCoroutineScope()


    // TODO create AngleLine View model, or even better, a swipe view model that hosts all the swipe related settings
    val showLineObjectPreview by AngleLineSettingsStore.showLineObjectPreview.asState()
    val showAngleLineObjectPreview by AngleLineSettingsStore.showAngleLineObjectPreview.asState()
    val showStartObjectPreview by AngleLineSettingsStore.showStartObjectPreview.asState()
    val showEndObjectPreview by AngleLineSettingsStore.showEndObjectPreview.asState()


    val lineObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.line_object), mode = ExpandableSectionMode.Expandable)
    val angleObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.angle_object), mode = ExpandableSectionMode.Expandable)
    val startObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.start_object), mode = ExpandableSectionMode.Expandable)
    val endObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.end_object), mode = ExpandableSectionMode.Expandable)

    val order by rememberLineObjectsOrder()
    var showOrderDialog by remember { mutableStateOf(false) }

    val lineObjects = rememberAngleLineObjects()

    // Instant mutators to avoid I/O overhead
    var mutableLineObject by remember(lineObjects.line) { mutableStateOf(lineObjects.line) }
    var mutableAngleLineObject by remember(lineObjects.angleLine) { mutableStateOf(lineObjects.angleLine) }
    var mutableStartObject by remember(lineObjects.startLine) { mutableStateOf(lineObjects.startLine) }
    var mutableEndObject by remember(lineObjects.endLine) { mutableStateOf(lineObjects.endLine) }


    val rgbLine by AngleLineSettingsStore.rgbLine.asState()


    val start = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val end = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val angleDeg = angle360FromOffset(start.value, end.value)

    var moveStartOrEnd by remember { mutableStateOf(false) }

    val sweepState = rememberSweepAngle()

    LaunchedEffect(angleDeg) {
        sweepState.onAngleChanged(angleDeg)
    }

    val sweep = sweepState.sweepAngle()

    val pickedRememberShapeAngle = remember(mutableAngleLineObject.shape) {
        mutableAngleLineObject.shape.resolveShape()
    }
    val pickedRememberRotationAngle = remember(mutableAngleLineObject.rotation) {
        mutableAngleLineObject.rotation.takeIf { it != -1 } ?: (0..360).random()
    }

    val pickedRememberShapeStart = remember(mutableStartObject.shape) {
        mutableStartObject.shape.resolveShape()
    }
    val pickedRememberRotationStart = remember(mutableStartObject.rotation) {
        mutableStartObject.rotation.takeIf { it != -1 } ?: (0..360).random()
    }

    val pickedRememberShapeEnd = remember(mutableEndObject.shape) {
        mutableEndObject.shape.resolveShape()
    }
    val pickedRememberRotationEnd = remember(mutableEndObject.rotation) {
        mutableEndObject.rotation.takeIf { it != -1 } ?: (0..360).random()
    }

    fun saveAll() {
        scope.launch {
            AngleLineSettingsStore.lineJson.set(ctx, CustomObjectJson.encode(mutableLineObject))
            AngleLineSettingsStore.angleLineJson.set(ctx, CustomObjectJson.encode(mutableAngleLineObject))
            AngleLineSettingsStore.startLineJson.set(ctx, CustomObjectJson.encode(mutableStartObject))
            AngleLineSettingsStore.endLineJson.set(ctx, CustomObjectJson.encode(mutableEndObject))
        }
    }

    Canvas(
        modifier = Modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .fillMaxSize()

    ) {
        val lineColor =
            if (rgbLine) Color.hsv(sweepState.angle360(), 1f, 1f)
            else extraColors.angleLine

        actionLine(
            start = start.value,
            end = end.value,
            sweepAngle = sweep,
            lineColor = lineColor,
            order = order,
            showLineObjectPreview = showLineObjectPreview,
            showAngleLineObjectPreview = showAngleLineObjectPreview,
            showStartObjectPreview = showStartObjectPreview,
            showEndObjectPreview = showEndObjectPreview,
            pickedRememberShapeAngle = pickedRememberShapeAngle,
            pickedRememberRotationAngle = pickedRememberRotationAngle,
            pickedRememberRotationStart = pickedRememberRotationStart,
            pickedRememberShapeStart = pickedRememberShapeStart,
            pickedRememberRotationEnd = pickedRememberRotationEnd,
            pickedRememberShapeEnd = pickedRememberShapeEnd,
            lineCustomObject = mutableLineObject,
            angleLineCustomObject = mutableAngleLineObject,
            startCustomObject = mutableStartObject,
            endCustomObject = mutableEndObject
        )
    }


    SettingsScaffold(
        title = stringResource(R.string.angle_line),
        onBack = {
            saveAll()
            onBack()
        },
        helpText = stringResource(R.string.angle_line_help),
        onReset = {
            scope.launch {
                AngleLineSettingsStore.resetAll(ctx)
            }
        },
        moreOptions = { dismiss ->
            listOf(
                MoreOptions(
                    text = { stringResource(R.string.configure_draw_order) },
                    onClick = {
                        showOrderDialog = true
                        dismiss()
                    },
                    icon = R.drawable.height,
                )
            )
        },
        topContent = {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { position: Offset ->
                                val distanceToStart = start.value distanceSquaredTo position
                                val distanceToEnd = end.value distanceSquaredTo position

                                moveStartOrEnd = if (distanceToEnd < distanceToStart) {
                                    false
                                } else {
                                    true
                                }
                            },
                            onDrag = { change, _ ->
                                scope.launch {
                                    if (moveStartOrEnd) {
                                        start.animateTo(
                                            targetValue = change.position,
                                            animationSpec = bouncySpec()
                                        )
                                    } else {
                                        end.animateTo(
                                            targetValue = change.position,
                                            animationSpec = bouncySpec()
                                        )
                                    }
                                }
                            }
                        )
                    }
                    .onGloballyPositioned { coordinates ->
                        val rect = coordinates.boundsInRoot()
                        val rectSize = (rect.height * density.density).toInt() / 4

                        scope.launch {
                            start.animateTo(
                                targetValue = Offset(
                                    rect.center.x + (-rectSize..rectSize).random(),
                                    rect.center.y + (-rectSize..rectSize).random()
                                ),
                                animationSpec = bouncySpec()
                            )
                        }

                        scope.launch {
                            end.animateTo(
                                targetValue = Offset(
                                    rect.center.x + (-rectSize..rectSize).random(),
                                    rect.center.y + (-rectSize..rectSize).random()
                                ),
                                animationSpec = bouncySpec()
                            )
                        }
                    }
            )
        }
    ) {
        ExpandableSection(lineObjectExpandableSectionState) {
            SettingsSwitchRow(AngleLineSettingsStore.showLineObjectPreview)

            AnimatedVisibility(showLineObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableLineObject,
                    default = defaultLineCustomObject,
                    properties = CustomObjectBlockProperties(
                        allowSizeCustomization = false,
                        allowShapeCustomization = false,
                        allowRotationCustomization = false
                    )
                ) { mutableLineObject = it }
            }
        }

        ExpandableSection(angleObjectExpandableSectionState) {
            SettingsSwitchRow(AngleLineSettingsStore.showAngleLineObjectPreview)

            AnimatedVisibility(showAngleLineObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableAngleLineObject,
                    default = defaultAngleCustomObject
                ) { mutableAngleLineObject = it }
            }
        }

        ExpandableSection(startObjectExpandableSectionState) {
            SettingsSwitchRow(AngleLineSettingsStore.showStartObjectPreview)

            AnimatedVisibility(showStartObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableStartObject,
                    default = defaultStartCustomObject
                ) { mutableStartObject = it }
            }
        }

        ExpandableSection(endObjectExpandableSectionState) {
            SettingsSwitchRow(AngleLineSettingsStore.showEndObjectPreview)

            AnimatedVisibility(showEndObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableEndObject,
                    default = defaultEndCustomObject
                ) { mutableEndObject = it }
            }
        }

        DragonSettingsGroup(
            title = R.string.other,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SettingsSwitchRow(AngleLineSettingsStore.rgbLine)
            SettingsColorPicker(ColorSettingsStore.angleLineColor)
        }
    }

    if (showOrderDialog) {
        AngleLineObjectsOrderDialog { showOrderDialog = false }
    }
}
