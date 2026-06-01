@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
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
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.common.serializables.CustomObjectBlockProperties
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dialogs.AngleLineObjectsOrderDialog
import org.elnix.dragonlauncher.ui.dialogs.rememberLineObjectsOrder
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.actionLine
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson.rememberAngleLineObjects
import org.elnix.dragonlauncher.ui.remembers.rememberSweepAngle
import kotlin.math.atan2


@Composable
fun AngleLineTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val extraColors = LocalExtraColors.current
    val scope = rememberCoroutineScope()

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


    val rgbLine by UiSettingsStore.rgbLine.asState()


    var dummyEnd by remember { mutableStateOf(Offset.Infinite) }
    var hasAlreadyBeenPlaced by remember { mutableStateOf(false) }


    var start by remember { mutableStateOf(Offset(0f, 0f)) }

    val dx = dummyEnd.x - start.x
    val dy = dummyEnd.y - start.y

    // angle relative to UP = 0°
    val angleRad = atan2(dx.toDouble(), -dy.toDouble())
    val angleDeg = Math.toDegrees(angleRad).toFloat()


    val sweepState = rememberSweepAngle()

    LaunchedEffect(angleDeg) {
        sweepState.onAngleChanged(angleDeg)
    }


    val sweep = sweepState.sweepAngle()


    val pickedRememberShapeAngle = remember(mutableAngleLineObject.shape) {
        (mutableAngleLineObject.shape ?: UiConstants.defaultAngleCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationAngle = remember(mutableAngleLineObject.rotation) {
        mutableAngleLineObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }

    val pickedRememberShapeStart = remember(mutableStartObject.shape) {
        (mutableStartObject.shape ?: UiConstants.defaultStartCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationStart = remember(mutableStartObject.rotation) {
        mutableStartObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }

    val pickedRememberShapeEnd = remember(mutableEndObject.shape) {
        (mutableEndObject.shape ?: UiConstants.defaultEndCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationEnd = remember(mutableEndObject.rotation) {
        mutableEndObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }


    fun saveAll() {
        scope.launch {
            AngleLineSettingsStore.lineJson.set(ctx, CustomObjectJson.encode(mutableLineObject))
            AngleLineSettingsStore.angleLineJson.set(ctx, CustomObjectJson.encode(mutableAngleLineObject))
            AngleLineSettingsStore.startLineJson.set(ctx, CustomObjectJson.encode(mutableStartObject))
            AngleLineSettingsStore.endLineJson.set(ctx, CustomObjectJson.encode(mutableEndObject))
        }
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
        otherIcons = arrayOf(
            Triple({ showOrderDialog = true }, R.drawable.more_vert, stringResource(R.string.more))
        ),
        topContent = {
            Canvas(
                modifier = Modifier
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        // Allow the user to move the end for cleaner preview
                        detectDragGestures { change, _ ->
                            dummyEnd = change.position
                        }
                        detectTapGestures { position ->
                            dummyEnd = position
                        }
                    }
                    .onGloballyPositioned { coordinates ->
                        if (!hasAlreadyBeenPlaced) {
                            val rect = coordinates.boundsInRoot()
                            val rectSize = (rect.height * density.density).toInt() / 2

                            dummyEnd = Offset(
                                rect.left + (0..rectSize).random(),
                                rect.top + (0..rectSize).random()
                            )
                            // Prevent the thing to move after first placement
                            hasAlreadyBeenPlaced = true
                        }
                    }
            ) {

                start = Offset(size.width / 2f, size.height / 2f)

                val lineColor =
                    if (rgbLine) Color.hsv(sweepState.angle360(), 1f, 1f)
                    else extraColors.angleLine

                actionLine(
                    start = start,
                    end = dummyEnd,
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
        }
    ) {
        /** Line object setting */
        ExpandableSection(lineObjectExpandableSectionState) {
            SettingsSwitchRow(
                setting = AngleLineSettingsStore.showLineObjectPreview,
                title = stringResource(R.string.show_app_line_preview),
                description = stringResource(R.string.show_app_line_preview_description)
            )
            AnimatedVisibility(showLineObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableLineObject,
                    default = UiConstants.defaultLineCustomObject,
                    properties = CustomObjectBlockProperties(
                        allowSizeCustomization = false,
                        allowShapeCustomization = false,
                        allowRotationCustomization = false
                    )
                ) { mutableLineObject = it }
            }
        }

        /** Angle Line object setting */
        ExpandableSection(angleObjectExpandableSectionState) {
            SettingsSwitchRow(
                setting = AngleLineSettingsStore.showAngleLineObjectPreview,
                title = stringResource(
                    R.string.show_app_angle_preview,
                    if (!showAngleLineObjectPreview) stringResource(R.string.do_you_hate_it) else ""
                ),
                description = stringResource(R.string.show_app_angle_preview_description)
            )

            AnimatedVisibility(showAngleLineObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableAngleLineObject,
                    default = UiConstants.defaultAngleCustomObject
                ) { mutableAngleLineObject = it }
            }
        }

        /** Start object setting */
        ExpandableSection(startObjectExpandableSectionState) {
            SettingsSwitchRow(
                setting = AngleLineSettingsStore.showStartObjectPreview,
                title = stringResource(R.string.show_start_object_preview),
                description = stringResource(R.string.show_start_object_preview_desc)
            )

            AnimatedVisibility(showStartObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableStartObject,
                    default = UiConstants.defaultStartCustomObject
                ) { mutableStartObject = it }
            }
        }

        /** End object setting */
        ExpandableSection(endObjectExpandableSectionState) {
            SettingsSwitchRow(
                setting = AngleLineSettingsStore.showEndObjectPreview,
                title = stringResource(R.string.show_end_object_preview),
                description = stringResource(R.string.show_end_object_preview_desc)
            )

            AnimatedVisibility(showEndObjectPreview) {
                EditCustomObjectBlock(
                    editObject = mutableEndObject,
                    default = UiConstants.defaultEndCustomObject
                ) { mutableEndObject = it }
            }
        }
    }

    if (showOrderDialog) {
        AngleLineObjectsOrderDialog { showOrderDialog = false }
    }
}
