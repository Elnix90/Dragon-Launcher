package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.enumsui.select.AngleObject
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
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.components.VerticalDragZone
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dialogs.AngleLineObjectsOrderDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock
import org.elnix.dragonlauncher.ui.helpers.customobjects.actionLine
import org.elnix.dragonlauncher.ui.helpers.customobjects.resolveRotation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.angle360
import org.elnix.dragonlauncher.ui.remembers.rememberSweepAngle

@Composable
fun AngleLineTab(
    swipeViewModel: SwipeViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val extraColors = LocalExtraColors.current
    val scope = rememberCoroutineScope()

    val backgroundColor = MaterialTheme.colorScheme.background

    val showLineObjectPreview by AngleLineSettingsStore.showLineObjectPreview.asState()
    val showAngleLineObjectPreview by AngleLineSettingsStore.showAngleLineObjectPreview.asState()
    val showStartObjectPreview by AngleLineSettingsStore.showStartObjectPreview.asState()
    val showEndObjectPreview by AngleLineSettingsStore.showEndObjectPreview.asState()
    val rgbLine by AngleLineSettingsStore.rgbLine.asState()

    var currentEditObject by remember { mutableStateOf(AngleObject.Line) }

    val lineObject by swipeViewModel.lineObject.asState()
    val angleObject by swipeViewModel.angleObject.asState()
    val startObject by swipeViewModel.startObject.asState()
    val endObject by swipeViewModel.endObject.asState()

    val startOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val endOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val angleDeg = angle360FromOffset(startOffset.value, endOffset.value)

    var moveStartOrEnd by remember { mutableStateOf(false) }

    val order by swipeViewModel.lineObjectOrder.asState()
    var showOrderDialog by remember { mutableStateOf(false) }

    val sweepState = rememberSweepAngle()

    LaunchedEffect(angleDeg) {
        sweepState.onAngleChanged(angleDeg)
    }

    val sweepAngle = sweepState.sweepAngle()
    val sweep = sweepAngle.toInt()

    val pickedRememberShapeAngle = remember(angleObject.shape) { angleObject.shape.resolveShape() }
    val pickedRememberRotationAngle = angleObject.resolveRotation(true, sweep)

    val pickedRememberShapeStart = remember(startObject.shape) { startObject.shape.resolveShape() }
    val pickedRememberRotationStart = startObject.resolveRotation(true, sweep)

    val pickedRememberShapeEnd = remember(endObject.shape) { endObject.shape.resolveShape() }
    val pickedRememberRotationEnd = endObject.resolveRotation(false, sweep)

    Canvas(Modifier.fillMaxSize()) {
        /**
         * The line color uses a [Int] angle, that it converts to a float, to prevent tiny difference in colors.
         * This method can only produce at most 360 different colors.
         *
         * This is needed by the [org.elnix.dragonlauncher.ui.helpers.customobjects.customGlowPaint] to provide optimizations when dealing with the low-level Paint APIs.
         * This prevents the [org.elnix.dragonlauncher.ui.helpers.customobjects.PaintCache] to be made useless by too much different [android.graphics.Paint] requests
         */
        val lineColor: Color =
            if (rgbLine) Color.hsv(sweepState.sweepAngle().angle360().toFloat(), 1f, 1f)
            else extraColors.angleLine

        actionLine(
            start = startOffset.value,
            end = endOffset.value,
            sweepAngle = sweepAngle,
            lineColor = lineColor,
            order = order,
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
            lineCustomObject = lineObject,
            angleLineCustomObject = angleObject,
            startCustomObject = startObject,
            endCustomObject = endObject
        )
    }

    SettingsScaffold(
        title = stringResource(R.string.angle_line),
        onBack = {
            swipeViewModel.saveLineObjects()
            swipeViewModel.saveOrder()
            navigator.onBack()
        },
        helpText = stringResource(R.string.angle_line_help),
        resetText = stringResource(R.string.reset_angle_tab),
        onReset = {
            swipeViewModel.resetLineObjects()
            swipeViewModel.resetOrder()
        },
        specialSettingsTitleContent = {
            AnimatedFab(
                onClick = { showOrderDialog = true },
                icon = R.drawable.height
            )
        },
        scrollableContent = false,
        topContent = {
            var height by remember { mutableIntStateOf(0) }
            var isFirstPositioning by remember { mutableStateOf(true) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.toDp)
                    .onGloballyPositioned { layoutCoordinates ->
                        if (isFirstPositioning) {
                            height = layoutCoordinates.size.width
                            isFirstPositioning = false
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { position: Offset ->
                                val distanceToStart = startOffset.value distanceSquaredTo position
                                val distanceToEnd = endOffset.value distanceSquaredTo position

                                moveStartOrEnd = if (distanceToEnd < distanceToStart) {
                                    false
                                } else {
                                    true
                                }
                            },
                            onDrag = { change, _ ->
                                scope.launch {
                                    if (moveStartOrEnd) {
                                        startOffset.animateTo(
                                            targetValue = change.position,
                                            animationSpec = bouncySpec()
                                        )
                                    } else {
                                        endOffset.animateTo(
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
                        val rectHeight = (rect.height / 2.5f).toInt()
                        val rectWidth = (rect.width / 2.5f).toInt()

                        fun randomPosition(): Offset = Offset(
                            rect.center.x + (-rectWidth..rectWidth).random(),
                            rect.center.y + (-rectHeight..rectHeight).random()
                        )

                        scope.launch {
                            startOffset.animateTo(
                                targetValue = randomPosition(),
                                animationSpec = bouncySpec()
                            )
                        }

                        scope.launch {
                            endOffset.animateTo(
                                targetValue = randomPosition(),
                                animationSpec = bouncySpec()
                            )
                        }
                    }
            )

            VerticalDragZone { height += it.toInt() }
        }
    ) {

        SingleSelectConnectedButtonRow(
            entries = AngleObject.entries,
            checked = { currentEditObject == it }
        ) { currentEditObject = it }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            AnimatedContent(currentEditObject) { currentEditObject ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    when (currentEditObject) {
                        AngleObject.Line -> {
                            DragonSettingsGroup { Setting(AngleLineSettingsStore.showLineObjectPreview) }

                            AnimatedVisibility(showLineObjectPreview) {
                                EditCustomObjectBlock(
                                    title = R.string.line_object,
                                    editObject = lineObject,
                                    default = defaultLineCustomObject,
                                    properties = CustomObjectBlockProperties(
                                        allowSizeCustomization = false,
                                        allowShapeCustomization = false,
                                        allowRotationCustomization = false,
                                        allowAlignCustomization = false
                                    )
                                ) { swipeViewModel.lineObject.value = it }
                            }
                        }

                        AngleObject.Angle -> {

                            DragonSettingsGroup { Setting(AngleLineSettingsStore.showAngleLineObjectPreview) }

                            AnimatedVisibility(showAngleLineObjectPreview) {
                                EditCustomObjectBlock(
                                    title = R.string.angle_object,
                                    editObject = angleObject,
                                    default = defaultAngleCustomObject
                                ) { swipeViewModel.angleObject.value = it }
                            }
                        }

                        AngleObject.Start -> {

                            DragonSettingsGroup { Setting(AngleLineSettingsStore.showStartObjectPreview) }

                            AnimatedVisibility(showStartObjectPreview) {
                                EditCustomObjectBlock(
                                    title = R.string.start_object,
                                    editObject = startObject,
                                    default = defaultStartCustomObject
                                ) { swipeViewModel.startObject.value = it }
                            }
                        }

                        AngleObject.End -> {

                            DragonSettingsGroup { Setting(AngleLineSettingsStore.showEndObjectPreview) }

                            AnimatedVisibility(showEndObjectPreview) {
                                EditCustomObjectBlock(
                                    title = R.string.end_object,
                                    editObject = endObject,
                                    default = defaultEndCustomObject
                                ) { swipeViewModel.endObject.value = it }
                            }
                        }
                    }
                }
            }

            DragonSettingsGroup(R.string.other) {
                Setting(AngleLineSettingsStore.rgbLine)
                Setting(AngleLineSettingsStore.startAndAngleShareSameRandomAngle)
                Setting(UiSettingsStore.linePreviewSnapToAction) { enabled ->
                    if (!enabled) {
                        scope.launch {
                            UiSettingsStore.animationWhenSnapping.set(ctx, false)
                        }
                    }
                }
                val snap by UiSettingsStore.linePreviewSnapToAction.asState()
                Setting(UiSettingsStore.animationWhenSnapping, enabled = snap)
                Setting(AngleLineSettingsStore.useSnappedAngleOrRealAngle, enabled = snap)
                Setting(ColorSettingsStore.angleLineColor)
            }
        }
    }

    if (showOrderDialog) {
        AngleLineObjectsOrderDialog(
            order = order,
            onChange = { swipeViewModel.lineObjectOrder.value = it }
        ) { swipeViewModel.saveOrder() }
    }
}
