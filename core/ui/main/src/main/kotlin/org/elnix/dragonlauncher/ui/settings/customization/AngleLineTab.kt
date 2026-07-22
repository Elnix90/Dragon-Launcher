package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultEndCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultLineCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultStartCustomObject
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.angle360FromOffset
import org.elnix.dragonlauncher.ktx.distanceSquaredTo
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.objects.AngleObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.EndObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.LineObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.StartObjectSettingStore
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.AngleLineObjectsOrderDialog
import org.elnix.dragonlauncher.ui.dialogs.rememberLineObjectsOrder
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.actionLine
import org.elnix.dragonlauncher.ui.helpers.customobjects.resolveRotation
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

    val backgroundColor = MaterialTheme.colorScheme.background

    // TODO create AngleLine View model, or even better, a swipe view model that hosts all the swipe related settings
    val showLineObjectPreview by AngleLineSettingsStore.showLineObjectPreview.asState()
    val showAngleLineObjectPreview by AngleLineSettingsStore.showAngleLineObjectPreview.asState()
    val showStartObjectPreview by AngleLineSettingsStore.showStartObjectPreview.asState()
    val showEndObjectPreview by AngleLineSettingsStore.showEndObjectPreview.asState()
    val rgbLine by AngleLineSettingsStore.rgbLine.asState()

    val lineObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.line_object), mode = ExpandableSectionMode.Expandable)
    val angleObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.angle_object), mode = ExpandableSectionMode.Expandable)
    val startObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.start_object), mode = ExpandableSectionMode.Expandable)
    val endObjectExpandableSectionState = rememberExpandableSection(stringResource(R.string.end_object), mode = ExpandableSectionMode.Expandable)

    val lineObjects = rememberAngleLineObjects()

    var mutableLineObject by remember(lineObjects.line) { mutableStateOf(lineObjects.line) }
    var mutableAngleLineObject by remember(lineObjects.angleLine) { mutableStateOf(lineObjects.angleLine) }
    var mutableStartObject by remember(lineObjects.startLine) { mutableStateOf(lineObjects.startLine) }
    var mutableEndObject by remember(lineObjects.endLine) { mutableStateOf(lineObjects.endLine) }

    val start = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val end = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val angleDeg = angle360FromOffset(start.value, end.value)

    var moveStartOrEnd by remember { mutableStateOf(false) }

    val order by rememberLineObjectsOrder()
    var orderMutable by remember { mutableStateOf(order) }

    var showOrderDialog by remember { mutableStateOf(false) }

    val sweepState = rememberSweepAngle()

    LaunchedEffect(angleDeg) {
        sweepState.onAngleChanged(angleDeg)
    }

    val sweepAngle = sweepState.sweepAngle()
    val sweep = sweepAngle.toInt()

    val pickedRememberShapeAngle = remember(mutableAngleLineObject.shape) { mutableAngleLineObject.shape.resolveShape() }
    val pickedRememberRotationAngle = mutableAngleLineObject.resolveRotation(true, sweep)

    val pickedRememberShapeStart = remember(mutableStartObject.shape) { mutableStartObject.shape.resolveShape() }
    val pickedRememberRotationStart = mutableStartObject.resolveRotation(true, sweep)

    val pickedRememberShapeEnd = remember(mutableEndObject.shape) { mutableEndObject.shape.resolveShape() }
    val pickedRememberRotationEnd = mutableEndObject.resolveRotation(false, sweep)

    Canvas(Modifier.fillMaxSize()) {
        val lineColor =
            if (rgbLine) Color.hsv(sweepState.angle360(), 1f, 1f)
            else extraColors.angleLine

        actionLine(
            start = start.value,
            end = end.value,
            sweepAngle = sweepAngle,
            lineColor = lineColor,
            order = orderMutable,
            eraseColor = backgroundColor,
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
            scope.launch {
                if (mutableEndObject != defaultLineCustomObject) {
                    LineObjectSettingStore.jsonSetting.set(ctx, CustomObjectJson.encode(mutableLineObject))
                }

                if (mutableAngleLineObject != defaultAngleCustomObject) {
                    AngleObjectSettingStore.jsonSetting.set(ctx, CustomObjectJson.encode(mutableAngleLineObject))
                }

                if (mutableStartObject != defaultStartCustomObject) {
                    StartObjectSettingStore.jsonSetting.set(ctx, CustomObjectJson.encode(mutableStartObject))
                }

                if (mutableEndObject != defaultEndCustomObject) {
                    EndObjectSettingStore.jsonSetting.set(ctx, CustomObjectJson.encode(mutableEndObject))
                }
                onBack()
            }
        },
        helpText = stringResource(R.string.angle_line_help),
        resetText = stringResource(R.string.reset_angle_tab),
        onReset = {
            scope.launch {
                AngleLineSettingsStore.resetAll(ctx)

                LineObjectSettingStore.resetAll(ctx)
                AngleObjectSettingStore.resetAll(ctx)
                StartObjectSettingStore.resetAll(ctx)
                EndObjectSettingStore.resetAll(ctx)
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
            DragonSettingsGroup { Setting(AngleLineSettingsStore.showLineObjectPreview) }

            AnimatedVisibility(showLineObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableLineObject,
                    default = defaultLineCustomObject,
                    properties = CustomObjectBlockProperties(
                        allowSizeCustomization = false,
                        allowShapeCustomization = false,
                        allowRotationCustomization = false,
                        allowAlignCustomization = false
                    )
                ) { mutableLineObject = it }
            }
        }
        ExpandableSection(angleObjectExpandableSectionState) {
            DragonSettingsGroup { Setting(AngleLineSettingsStore.showAngleLineObjectPreview) }

            AnimatedVisibility(showAngleLineObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableAngleLineObject,
                    default = defaultAngleCustomObject
                ) { mutableAngleLineObject = it }
            }
        }

        ExpandableSection(startObjectExpandableSectionState) {
            DragonSettingsGroup { Setting(AngleLineSettingsStore.showStartObjectPreview) }

            AnimatedVisibility(showStartObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableStartObject,
                    default = defaultStartCustomObject
                ) { mutableStartObject = it }
            }
        }

        ExpandableSection(endObjectExpandableSectionState) {
            DragonSettingsGroup { Setting(AngleLineSettingsStore.showEndObjectPreview) }

            AnimatedVisibility(showEndObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableEndObject,
                    default = defaultEndCustomObject
                ) { mutableEndObject = it }
            }
        }

        DragonSettingsGroup(R.string.other) {
            Setting(AngleLineSettingsStore.rgbLine)
            Setting(AngleLineSettingsStore.startAndAngleShareSameRandomAngle)
            Setting(ColorSettingsStore.angleLineColor)
        }
    }

    if (showOrderDialog) {
        AngleLineObjectsOrderDialog(
            order = order,
            onChange = {
                orderMutable = it
            }
        ) { showOrderDialog = false }
    }
}
