@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.UiCircle
import org.elnix.dragonlauncher.base.model.serializables.CustomHapticFeedback
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.defaultDragDistance
import org.elnix.dragonlauncher.common.circles.rotateBy
import org.elnix.dragonlauncher.enumsui.select.NestEditMode
import org.elnix.dragonlauncher.enumsui.select.NestEditMode.Drag
import org.elnix.dragonlauncher.enumsui.select.NestEditMode.Haptic
import org.elnix.dragonlauncher.enumsui.select.NestEditMode.MinAngle
import org.elnix.dragonlauncher.enumsui.select.NestEditMode.Other
import org.elnix.dragonlauncher.enumsui.select.NestEditMode.Radius
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.PointViewModel
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.defaultHapticFeedback
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedBackEditorButtonWithPlayTest
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedbackEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.nests.circlesSettingsOverlay
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.rememberSwipeDefaultParams


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun NestEditingScreen(
    nestId: Int?,
    pointViewModel: PointViewModel = activityViewModel(),
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val nests by pointViewModel.nests.collectAsState()
    val pointService = pointViewModel.pointsService

    if (nestId == null) return
    val currentNest = nests.find { it.id == nestId } ?: run {
        // The nest isn't found in the list, create a new one with this id
        scope.launch {
            pointService.addNest()
            ctx.showToast("Saved missing nest!")
        }

        onBack()
        return
    }


    val angleColor = MaterialTheme.colorScheme.tertiary

    val drawParams by rememberSwipeDefaultParams(
        backgroundColor = MaterialTheme.colorScheme.background,
        forceShowAllActionsInCurrentNest = true
    )

    val subNestDefaultRadius by SwipeMapSettingsStore.subNestDefaultRadius.asState()
    var tempRadius by remember { mutableStateOf(currentNest.nestRadius) }


    val dragDistancesState = remember(currentNest.id) {
        mutableStateMapOf<Int, Int>().apply {
            putAll(currentNest.dragDistances)
        }
    }

    var showHapticFeedbackEditor by remember { mutableStateOf<Int?>(null) }

    val minAngleState = remember(currentNest.id) {
        mutableStateMapOf<Int, Int>().apply {
            putAll(currentNest.minAngleActivation)
        }
    }

    val circlesRealSize = dragDistancesState.map { (id, radius) ->
        UiCircle(
            id = id,
            radius = radius.toFloat()
        )
    }

    fun updateNest(block: () -> Nest) {
        pointService.editNest(nestId) { block() }
    }

    fun commitDragDistances(state: Map<Int, Int>) {
        updateNest {
            currentNest.copy(dragDistances = state.toMap())
        }
    }

    fun commitHaptic(state: Map<Int, CustomHapticFeedback>) {
        updateNest {
            currentNest.copy(haptic = state.toMap())
        }
    }

    fun commitAngle(state: Map<Int, Int>) {
        updateNest {
            currentNest.copy(minAngleActivation = state.toMap())
        }
    }


    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var angle by remember { mutableFloatStateOf(0f) }


    SettingsScaffold(
        title = stringResource(R.string.edit_nest),
        onBack = onBack,
        helpText = stringResource(R.string.edit_nest_help),
        resetText = stringResource(R.string.reset_nest_text),
        onReset = {
            // Resets current nest to a new one, with the same id (avoids destroying it)
            pointService.editNest(nestId) { Nest(id = nestId) }
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, gestureZoom, gestureRotate ->
                        val oldScale = zoom
                        val newScale = zoom * gestureZoom

                        // For natural zooming and rotating, the centroid of the gesture should
                        // be the fixed point where zooming and rotating occurs.
                        // We compute where the centroid was (in the pre-transformed coordinate
                        // space), and then compute where it will be after this delta.
                        // We then compute what the new offset should be to keep the centroid
                        // visually stationary for rotating and zooming, and also apply the pan.
                        offset =
                            (offset + centroid / oldScale).rotateBy(gestureRotate) -
                                    (centroid / newScale + pan / oldScale)
                        zoom = newScale
                        angle += gestureRotate
                    }
                }
                .graphicsLayer {
                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    scaleX = zoom
                    scaleY = zoom
                    rotationZ = angle
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .drawWithCache {
                    onDrawBehind {
                        circlesSettingsOverlay(
                            drawParams = drawParams,
                            center = center,
                            depth = 1,
                            circles = circlesRealSize,
                            selectedPoint = null,
                            nestId = nestId,
                            preventBgErasing = true
                        )

                        // Show the min angle to activate
                        circlesRealSize.forEach { circle ->
                            val arcRadius = circle.radius + 10

                            val rect = Rect(
                                center.x - arcRadius,
                                center.y - arcRadius,
                                center.x + arcRadius,
                                center.y + arcRadius
                            )

                            drawArc(
                                color = angleColor,
                                startAngle = -90f,
                                sweepAngle = minAngleState[circle.id]?.toFloat() ?: 0f,
                                useCenter = false,
                                topLeft = rect.topLeft,
                                size = Size(rect.width, rect.height),
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                }
        )


        val pagerState = rememberPagerState { 5 }

        DragonColumnGroup {
            MultiSelectConnectedButtonRow(
                entries = NestEditMode.entries,
                checked = { pagerState.currentPage == it.ordinal }
            ) {
                scope.launch {
                    pagerState.animateScrollToPage(it.ordinal)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) { page ->
                val currentPage = NestEditMode.entries[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentPage) {

                        Drag -> {
                            dragDistancesState.toSortedMap().forEach { (index, distance) ->
                                SliderWithLabel(
                                    label = if (index == -1) stringResource(R.string.cancel_zone)
                                    else "${stringResource(R.string.circle)}: $index",
                                    value = distance,
                                    valueRange = 0..1000,
                                    showValue = true,
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    onReset = {
                                        dragDistancesState[index] = defaultDragDistance(index)
                                        commitDragDistances(dragDistancesState)
                                    },
                                    onDragStateChange = { isDragging ->
                                        if (!isDragging) {
                                            commitDragDistances(dragDistancesState)
                                        }
                                    }
                                ) { newValue ->
                                    dragDistancesState[index] = newValue
                                }
                            }
                        }

                        Haptic -> {
                            // Keep drag distance state here cause haptic may be empty dues to how it is handled
                            dragDistancesState.toSortedMap().filter { it.key != -1 }
                                .forEach { (idx, _) ->

                                    HapticFeedBackEditorButtonWithPlayTest(
                                        customHapticFeedback = currentNest.haptic[idx] ?: defaultHapticFeedback(idx),
                                        titleExt = ": $idx",
                                        onClick = { showHapticFeedbackEditor = idx },
                                    )
                                }
                        }

                        MinAngle -> {
                            dragDistancesState.toSortedMap().filter { it.key != -1 }
                                .forEach { (index, _) ->
                                    val angle = minAngleState[index] ?: 0
                                    SliderWithLabel(
                                        label = "${stringResource(R.string.min_angle_to_activate)}: $index",
                                        value = angle,
                                        valueRange = 0..360,
                                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                        onReset = {
                                            minAngleState[index] = 0
                                            commitAngle(minAngleState)
                                        },
                                        onDragStateChange = { isDragging ->
                                            if (!isDragging) {
                                                commitAngle(minAngleState)
                                            }
                                        }
                                    ) { newValue ->
                                        minAngleState[index] = newValue
                                    }
                                }
                        }

                        // Well in this tab I'll just put whatever settings I can put
                        Radius -> {
                            SliderWithLabel(
                                label = stringResource(R.string.nest_radius),
                                value = tempRadius ?: subNestDefaultRadius,
                                valueRange = 0..50,
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                onReset = {
                                    updateNest { currentNest.copy(nestRadius = null) }
                                    tempRadius = subNestDefaultRadius
                                },
                                onDragStateChange = { isDragging ->
                                    if (!isDragging) {
                                        updateNest {
                                            currentNest.copy(nestRadius = tempRadius)
                                        }
                                    }
                                }
                            ) { newValue -> tempRadius = newValue }

                            // Used to control whether the nest displays its circle individually or not
                            SwitchRow(
                                state = currentNest.showCircle ?: drawParams.showAppCirclePreview,
                                title = stringResource(R.string.show_circle),
                                onReset = { updateNest { currentNest.copy(showCircle = null) } }
                            ) { updateNest { currentNest.copy(showCircle = it) } }
                        }

                        Other -> {
                            SwitchRow(
                                state = currentNest.showAllActionsOnCurrentCircle ?: drawParams.showAllActionsOnCurrentCircle,
                                title = stringResource(R.string.show_all_actions_on_current_circle),
                                description = stringResource(R.string.show_all_actions_on_current_circle_description),
                                onReset = {
                                    updateNest {
                                        currentNest.copy(showAllActionsOnCurrentCircle = null)
                                    }
                                }
                            ) { showAllActionsOnCurrentCircle ->
                                updateNest {
                                    currentNest.copy(showAllActionsOnCurrentCircle = showAllActionsOnCurrentCircle)
                                }
                            }

                            SwitchRow(
                                state = currentNest.showAllActionsOnCurrentNest ?: drawParams.showAllActionsOnCurrentNest,
                                title = stringResource(R.string.show_all_actions_on_current_nest),
                                description = stringResource(R.string.show_all_actions_on_current_nest_desc),
                                onReset = {
                                    updateNest {
                                        currentNest.copy(showAllActionsOnCurrentNest = null)
                                    }
                                }
                            ) { showAllActionsOnCurrentNest ->
                                updateNest {
                                    currentNest.copy(showAllActionsOnCurrentNest = showAllActionsOnCurrentNest)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHapticFeedbackEditor != null) {
        val circleIdToEdit = showHapticFeedbackEditor!!

        HapticFeedbackEditor(
            initial = currentNest.haptic[circleIdToEdit],
            onDismiss = { showHapticFeedbackEditor = null }
        ) { newHaptic ->
            newHaptic?.let {
                commitHaptic(
                    state = currentNest.haptic + (circleIdToEdit to it)
                )
            }
            showHapticFeedbackEditor = null
        }
    }
}
