@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.CycleActionStage
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.base.util.ColorUtils.definedOrNull
import org.elnix.dragonlauncher.enumsui.select.PointFeaturePanel
import org.elnix.dragonlauncher.enumsui.select.SelectedUnselectedViewMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.PointPreviewCanvas
import org.elnix.dragonlauncher.ui.defaultHapticFeedback
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPointSheet(
    point: Point,
    isDefaultEditing: Boolean = false,
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    onDismiss: () -> Unit,
    onConfirm: (Point) -> Unit
) {
    val extraColors = LocalExtraColors.current
    val pointsService = pointsViewModel.pointsService

    val defaultPoint by pointsService.defaultPoint.asState()
    val nests by pointsService.nests.asState()

    var editPoint by remember { mutableStateOf(point) }
    var showEditIconDialog by remember { mutableStateOf(false) }
    var showEditActionDialog by remember { mutableStateOf(false) }
    var showShapePickerDialog by remember { mutableStateOf(false) }
    var showSelectedShapePickerDialog by remember { mutableStateOf(false) }
    var showHapticFeedbackEditor by remember { mutableStateOf(false) }

    var expandedFeaturePanel: PointFeaturePanel? by remember { mutableStateOf(null) }
    var showLiveNestNestPicker by remember { mutableStateOf(false) }
    var showHoldAndRunActionDialog by remember { mutableStateOf(false) }

    var editingCycleStageActionIndex by remember { mutableStateOf<Int?>(null) }
    var editingCycleStageHapticIndex by remember { mutableStateOf<Int?>(null) }


    val currentActionColor = editPoint.action.actionColor(extraColors)

    val label = editPoint.customName ?: actionLabel(editPoint.action)
    val actionColor = editPoint.action.actionColor(extraColors, editPoint.customActionColor?.let { Color(it) })


    val defaultBorderStroke =
        defaultPoint.borderStroke
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.borderStroke!!

    val defaultBorderColor =
        defaultPoint.borderColor
            ?.takeIf { !isDefaultEditing }
            ?.let(::Color)
            ?: extraColors.circle

    val defaultBackgroundColor =
        defaultPoint.backgroundColor
            ?.takeIf { !isDefaultEditing }
            ?.let(::Color)
            ?: Color.Unspecified

    val defaultBorderStrokeSelected =
        defaultPoint.borderStroke
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.borderStrokeSelected!!

    val defaultBorderColorSelected =
        defaultPoint.borderColorSelected
            ?.takeIf { !isDefaultEditing }
            ?.let(::Color)
            ?: extraColors.circle

    val defaultBackgroundColorSelected =
        defaultPoint.backgroundColorSelected
            ?.takeIf { !isDefaultEditing }
            ?.let(::Color)
            ?: Color.Unspecified

    val defaultSize =
        defaultPoint.size
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.size!!

    val defaultInnerPadding =
        defaultPoint.innerPadding
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.innerPadding!!

    val defaultLiveNestDelay =
        defaultPoint.liveNestPreviewDelayMs
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.liveNestPreviewDelayMs!!

    val defaultLiveNestScale =
        defaultPoint.liveNestScale
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.liveNestScale!!

    val defaultLiveNestMainNestOpacityPercent =
        defaultPoint.liveNestMainNestOpacityPercent
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.liveNestMainNestOpacityPercent!!

    val defaultLiveNestGraceDistance =
        defaultPoint.liveNestGraceDistancePx
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.liveNestGraceDistancePx!!

    val defaultLiveNestSnapsToFingerPosition =
        defaultPoint.liveNestSnapsToFingerPosition
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.liveNestSnapsToFingerPosition!!

    val defaultHoldAndRunDelayMs =
        defaultPoint.holdAndRunDelayMs
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.holdAndRunDelayMs!!

    val defaultLoopDelay =
        defaultPoint.cycleActionsLoopDelayMs
            ?.takeIf { !isDefaultEditing }
            ?: defaultSwipePointsValues.cycleActionsLoopDelayMs!!



    LaunchedEffect(
        editPoint.action,
        editPoint.customIcon,
        editPoint.customActionColor,
        editPoint.size,
        editPoint.cycleActions,
        editPoint.holdAndRunDelayMs,
        editPoint.size
    ) {
        iconsViewModel.reloadIcon(editPoint)
    }


    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.edit_point),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                )
                Spacer(Modifier.weight(1f))
                DragonIconButton(
                    icon = R.drawable.reset,
                    contentDescription = stringResource(R.string.reset)
                ) {
                    editPoint = Point(
                        offset = editPoint.offset,
                        nestId = editPoint.nestId,
                        action = editPoint.action,
                        id = editPoint.id
                    )
                }
            }

            DragonColumnGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = stringResource(R.string.unselected_action),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        text = stringResource(R.string.selected_action),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                PointPreviewCanvas(
                    editPoint = editPoint,
                    modifier = Modifier.fillMaxWidth(1f)
                )
            }
        }

        Spacer(5.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            DragonColumnGroup {
                SingleSelectConnectedButtonRow(
                    entries = PointFeaturePanel.entries,
                    checked = {
                        when (it) {
                            PointFeaturePanel.LiveNest -> editPoint.liveNestTargetNestId != null
                            PointFeaturePanel.CycleActions -> editPoint.cycleActions != null
                            PointFeaturePanel.HoldAndRun -> editPoint.holdAndRunDelayMs != null
                        }
                    }
                ) {
                    expandedFeaturePanel = it.takeIf {
                        expandedFeaturePanel != it
                    }
                }

                AnimatedContent(expandedFeaturePanel) { expandedFeature ->
                    when (expandedFeature) {
                        PointFeaturePanel.LiveNest -> {

                            val liveNestEnabled = editPoint.liveNestTargetNestId != null
                            val targetNest = nests.find { it.id == editPoint.liveNestTargetNestId }
                            val nestLabel = targetNest?.name ?: targetNest?.let { "Nest ${it.id}" } ?: ""

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                if (!liveNestEnabled && !isDefaultEditing) {
                                    DragonButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { showLiveNestNestPicker = true }
                                    ) {
                                        Text(stringResource(R.string.live_nest_pick_nest))
                                    }

                                } else {
                                    if (isDefaultEditing) {
                                        TextWithDescription(
                                            text = stringResource(R.string.default_point_live_nest_defaults),
                                            description = stringResource(R.string.default_point_live_nest_defaults_summary)
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            DragonButton(
                                                modifier = Modifier.weight(1f),
                                                onClick = { showLiveNestNestPicker = true }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.edit_rounded),
                                                    contentDescription = null
                                                )
                                                Spacer(5.dp)
                                                Text(
                                                    text = stringResource(R.string.live_nest_target_nest),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = nestLabel,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }

                                            DragonIconButton(
                                                icon = R.drawable.delete_forever,
                                                contentDescription = stringResource(R.string.disable)
                                            ) {
                                                editPoint = editPoint.copy(
                                                    liveNestTargetNestId = null,
                                                    liveNestPreviewDelayMs = null,
                                                    liveNestScale = null,
                                                    liveNestGraceDistancePx = null,
                                                    liveNestMainNestOpacityPercent = null
                                                )
                                            }
                                        }
                                    }


                                    /*   Hold delay slider   */
                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_hold_delay),
                                        value = editPoint.liveNestPreviewDelayMs ?: defaultLiveNestDelay,
                                        valueRange = 0..5000,
                                        color = MaterialTheme.colorScheme.primary,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestPreviewDelayMs = null)
                                        }
                                    ) { editPoint = editPoint.copy(liveNestPreviewDelayMs = it) }

                                    /*   Scale slider   */
                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_scale),
                                        value = editPoint.liveNestScale ?: defaultLiveNestScale,
                                        valueRange = 0.3f..1.0f,
                                        color = MaterialTheme.colorScheme.primary,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestScale = null)
                                        }
                                    ) { editPoint = editPoint.copy(liveNestScale = it) }

                                    /*   Grace distance slider   */
                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_grace_distance),
                                        value = editPoint.liveNestGraceDistancePx ?: defaultLiveNestGraceDistance,
                                        valueRange = 0..3000,
                                        color = MaterialTheme.colorScheme.primary,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestGraceDistancePx = null)
                                        }
                                    ) { editPoint = editPoint.copy(liveNestGraceDistancePx = it) }

                                    SwitchRow(
                                        state = editPoint.liveNestSnapsToFingerPosition ?: defaultLiveNestSnapsToFingerPosition,
                                        title = stringResource(R.string.live_nest_snaps_to_finger_position),
                                        description = stringResource(R.string.live_nest_snaps_to_finger_position_desc)
                                    ) { on ->
                                        editPoint = if (!on) {
                                            editPoint.copy(liveNestSnapsToFingerPosition = false)
                                        } else {
                                            editPoint.copy(liveNestSnapsToFingerPosition = null)
                                        }
                                    }

                                    val currentOpacity = (editPoint.liveNestMainNestOpacityPercent ?: defaultLiveNestMainNestOpacityPercent)
                                    SwitchRow(
                                        state = currentOpacity != -1,
                                        title = stringResource(R.string.live_nest_dim_main_nest),
                                        description = stringResource(R.string.live_nest_dim_main_nest_desc)
                                    ) { on ->
                                        editPoint = if (on) {
                                            editPoint.copy(liveNestMainNestOpacityPercent = defaultLiveNestMainNestOpacityPercent)
                                        } else {
                                            editPoint.copy(liveNestMainNestOpacityPercent = -1)
                                        }
                                    }

                                    AnimatedVisibility(currentOpacity != -1) {
                                        SliderWithLabel(
                                            label = stringResource(R.string.live_nest_main_nest_opacity),
                                            value = currentOpacity,
                                            valueRange = 0..100,
                                            color = MaterialTheme.colorScheme.primary,
                                            onReset = {
                                                editPoint = editPoint.copy(liveNestMainNestOpacityPercent = null)
                                            }
                                        ) { editPoint = editPoint.copy(liveNestMainNestOpacityPercent = it) }
                                    }
                                }
                            }
                        }

                        PointFeaturePanel.CycleActions -> {
                            val cycleStages = editPoint.cycleActions ?: emptyList()

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!isDefaultEditing) {
                                    cycleStages.forEachIndexed { index, stage ->
                                        val stageLabel = actionLabel(stage.action)
                                        val stageActionColor = stage.action.actionColor(extraColors)

                                        OutlinedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = DragonShape,
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            colors = CardDefaults.outlinedCardColors()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = stringResource(
                                                            R.string.cycle_actions_stage,
                                                            index + 1
                                                        ),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    DragonIconButton(
                                                        icon = R.drawable.close,
                                                        contentDescription = stringResource(R.string.disable)
                                                    ) {
                                                        val updated = cycleStages.toMutableList()
                                                            .also { it.removeAt(index) }
                                                        editPoint = if (updated.isEmpty()) {
                                                            editPoint.copy(
                                                                cycleActions = null,
                                                                cycleActionsLoopDelayMs = null
                                                            )
                                                        } else {
                                                            editPoint.copy(cycleActions = updated)
                                                        }
                                                    }
                                                }

                                                DragonRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    onClick = { editingCycleStageActionIndex = index }
                                                ) {
                                                    Text(
                                                        text = stageLabel,
                                                        color = stageActionColor,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Spacer()
                                                    Icon(
                                                        painter = painterResource(R.drawable.edit_rounded),
                                                        contentDescription = stringResource(R.string.edit_action),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                SliderWithLabel(
                                                    label = stringResource(R.string.cycle_actions_delay),
                                                    value = stage.triggerTimeMs,
                                                    valueRange = 100..5000,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    onReset = {
                                                        val updated = cycleStages.toMutableList().also {
                                                            it[index] = it[index].copy(
                                                                triggerTimeMs = defaultPoint.cycleActionStageDefaultDelay
                                                                    ?: defaultSwipePointsValues.cycleActionStageDefaultDelay!!
                                                            )
                                                        }
                                                        editPoint = editPoint.copy(cycleActions = updated)
                                                    }
                                                ) { newDelay ->
                                                    val updated = cycleStages.toMutableList().also {
                                                        it[index] = it[index].copy(triggerTimeMs = newDelay)
                                                    }
                                                    editPoint = editPoint.copy(cycleActions = updated)
                                                }

                                                HapticFeedBackEditorButtonWithPlayTest(
                                                    customHapticFeedback = stage.hapticFeedback ?: defaultHapticFeedback(),
                                                    titleExt = " (Stage ${index + 1})",
                                                    onClick = { editingCycleStageHapticIndex = index }
                                                )
                                            }
                                        }
                                    }

                                    // Add Stage (below the cards)
                                    DragonButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            val newStage = CycleActionStage(
                                                triggerTimeMs = 500,
                                                action = editPoint.action
                                            )
                                            editPoint = editPoint.copy(
                                                cycleActions = cycleStages + newStage
                                            )
                                        }
                                    ) {
                                        Text(stringResource(R.string.cycle_actions_add_stage))
                                    }
                                }

                                if (isDefaultEditing) {
                                    SliderWithLabel(
                                        label = stringResource(R.string.cycle_actions_stage_default_delay),
                                        value = editPoint.cycleActionStageDefaultDelay
                                            ?: defaultSwipePointsValues.cycleActionStageDefaultDelay!!,
                                        valueRange = 50..5000,
                                        color = MaterialTheme.colorScheme.primary,
                                        onReset = {
                                            editPoint = editPoint.copy(cycleActionStageDefaultDelay = null)
                                        }
                                    ) { ms ->
                                        editPoint = editPoint.copy(cycleActionStageDefaultDelay = ms)
                                    }
                                }

                                val currentLoopDelay = editPoint.cycleActionsLoopDelayMs ?: defaultLoopDelay
                                /*   Loop (optional tail before cycle restarts)   */
                                if (cycleStages.isNotEmpty() || isDefaultEditing) {
                                    SwitchRow(
                                        state = currentLoopDelay != -1,
                                        title = stringResource(R.string.cycle_actions_loop),
                                        description = stringResource(R.string.cycle_actions_loop_desc)
                                    ) { on ->
                                        editPoint = editPoint.copy(
                                            cycleActionsLoopDelayMs =
                                                if (on) defaultLoopDelay else -1
                                        )
                                    }

                                    AnimatedVisibility(currentLoopDelay != -1) {
                                        SliderWithLabel(
                                            label = stringResource(R.string.cycle_actions_loop_delay),
                                            value = currentLoopDelay,
                                            valueRange = 50..5000,
                                            color = MaterialTheme.colorScheme.primary,
                                            onReset = {
                                                editPoint = editPoint.copy(cycleActionsLoopDelayMs = null)
                                            }
                                        ) { ms ->
                                            editPoint = editPoint.copy(cycleActionsLoopDelayMs = ms)
                                        }
                                    }
                                }
                            }

                        }

                        PointFeaturePanel.HoldAndRun -> {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                if (isDefaultEditing) {
                                    val defaultHoldAndRunMs = editPoint.holdAndRunDelayMs ?: 1000
                                    Text(
                                        text = stringResource(R.string.default_hold_and_run_delay_summary),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    SliderWithLabel(
                                        label = stringResource(R.string.default_hold_and_run_delay),
                                        value = defaultHoldAndRunMs,
                                        valueRange = 100..5000,
                                        color = MaterialTheme.colorScheme.primary,
                                        onReset = {
                                            editPoint = editPoint.copy(holdAndRunDelayMs = null)
                                        }
                                    ) { editPoint = editPoint.copy(holdAndRunDelayMs = it) }

                                } else {
                                    val harEnabled = editPoint.holdAndRunDelayMs != null

                                    if (!harEnabled) {
                                        DragonButton(
                                            onClick = {
                                                editPoint = editPoint.copy(
                                                    holdAndRunDelayMs = defaultPoint.holdAndRunDelayMs ?: 1000
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(stringResource(R.string.hold_and_run_enable))
                                        }
                                    } else {
                                        SliderWithLabel(
                                            label = stringResource(R.string.hold_and_run_delay),
                                            value = editPoint.holdAndRunDelayMs ?: defaultHoldAndRunDelayMs,
                                            valueRange = 100..5000,
                                            color = MaterialTheme.colorScheme.primary,
                                            onReset = {
                                                editPoint = editPoint.copy(
                                                    holdAndRunDelayMs = null,
                                                    holdAndRunAction = null
                                                )
                                            }
                                        ) { newDelay ->
                                            editPoint = editPoint.copy(holdAndRunDelayMs = newDelay)
                                        }

                                        SwitchRow(
                                            state = editPoint.holdAndRunAction != null,
                                            title = stringResource(R.string.hold_and_run_custom_action),
                                            description = stringResource(R.string.hold_and_run_custom_action_desc)
                                        ) { on ->
                                            editPoint = if (on) {
                                                editPoint.copy(
                                                    holdAndRunAction = editPoint.holdAndRunAction
                                                        ?: editPoint.action
                                                )
                                            } else {
                                                editPoint.copy(holdAndRunAction = null)
                                            }
                                        }

                                        editPoint.holdAndRunAction?.let { harAction ->
                                            val harLabel = actionLabel(harAction)
                                            val harColor = harAction.actionColor(
                                                extraColors,
                                                editPoint.customActionColor?.let { Color(it) }
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(DragonShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .clickable { showHoldAndRunActionDialog = true }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = harLabel,
                                                    color = harColor,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(Modifier.weight(1f))
                                                Icon(
                                                    painter = painterResource(R.drawable.edit_rounded),
                                                    contentDescription = stringResource(R.string.edit_action),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                editPoint = editPoint.copy(
                                                    holdAndRunDelayMs = null,
                                                    holdAndRunAction = null
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text(
                                                text = stringResource(R.string.disable),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        null -> {}
                    }
                }
            }


            if (!isDefaultEditing) {
                DragonColumnGroup {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        DragonButton(
                            onClick = { showEditActionDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                color = actionColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(5.dp)
                            Icon(
                                painter = painterResource(R.drawable.edit_rounded),
                                contentDescription = stringResource(R.string.edit_action),
                                tint = actionColor
                            )
                        }

                        DragonButton(
                            onClick = { showEditIconDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.edit_icon))
                            Spacer(5.dp)
                            Icon(
                                painter = painterResource(R.drawable.edit_rounded),
                                contentDescription = stringResource(R.string.edit_action)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editPoint.customName ?: "",
                        onValueChange = {
                            editPoint = editPoint.copy(customName = it)
                        },
                        label = { Text(stringResource(R.string.custom_name)) },
                        trailingIcon = {
                            AnimatedVisibility(editPoint.customName != null) {
                                DragonIconButton(
                                    icon = R.drawable.reset,
                                    contentDescription = stringResource(R.string.reset)
                                ) {
                                    editPoint = editPoint.copy(customName = null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppObjectsColors.outlinedTextFieldColors()
                    )

                    ColorPickerRow(
                        title = stringResource(R.string.custom_action_color),
                        description = null,
                        currentColor = editPoint.customActionColor?.let { Color(it) }
                            ?: currentActionColor,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    ) { selectedColor ->
                        editPoint = editPoint.copy(customActionColor = selectedColor?.toArgb())
                    }
                }
            }

            DragonColumnGroup {
                SliderWithLabel(
                    label = stringResource(R.string.inner_padding),
                    value = editPoint.innerPadding ?: defaultInnerPadding,
                    valueRange = 0..100,
                    onReset = { editPoint = editPoint.copy(innerPadding = null) }
                ) { editPoint = editPoint.copy(innerPadding = it) }

                SliderWithLabel(
                    label = stringResource(R.string.size),
                    value = editPoint.size ?: defaultSize,
                    valueRange = 1..200,
                    onReset = { editPoint = editPoint.copy(size = null) }
                ) { editPoint = editPoint.copy(size = it) }
            }


            DragonColumnGroup {
                // Selected / Unselected Options Toggler
                var selectedView by remember { mutableStateOf(SelectedUnselectedViewMode.Selected) }

                SingleSelectConnectedButtonRow(
                    entries = SelectedUnselectedViewMode.entries,
                    checked = { selectedView == it },
                ) { selectedView = it }


                AnimatedContent(selectedView) { view ->
                    Column {
                        when (view) {

                            SelectedUnselectedViewMode.Unselected -> {
                                SliderWithLabel(
                                    label = stringResource(R.string.border_stroke),
                                    value = editPoint.borderStroke
                                        ?: defaultBorderStroke,
                                    valueRange = 0f..50f,
                                    color = MaterialTheme.colorScheme.primary,
                                    onReset = {
                                        editPoint = editPoint.copy(borderStroke = null)
                                    }
                                ) {
                                    editPoint = editPoint.copy(borderStroke = it)
                                }

                                ColorPickerRow(
                                    title = stringResource(R.string.border_color),
                                    description = null,
                                    currentColor = editPoint.borderColor?.let { Color(it) }
                                        ?: defaultBorderColor
                                ) { selectedColor ->
                                    editPoint = editPoint.copy(borderColor = selectedColor?.toArgb())
                                }

                                ColorPickerRow(
                                    title = stringResource(R.string.background_color),
                                    description = null,
                                    currentColor = editPoint.backgroundColor?.let { Color(it) }
                                        ?: defaultBackgroundColor
                                ) { selectedColor ->
                                    editPoint = editPoint.copy(
                                        backgroundColor = selectedColor.definedOrNull()
                                            ?.toArgb()
                                    )
                                }

                                ShapeRow(
                                    selected = editPoint.borderShape ?: defaultSwipePointsValues.borderShape!!,
                                    title = stringResource(R.string.edit_border_shape),
                                    onReset = {
                                        editPoint = editPoint.copy(borderShape = null)
                                    }
                                ) { showShapePickerDialog = true }
                            }

                            SelectedUnselectedViewMode.Selected -> {
                                SliderWithLabel(
                                    label = stringResource(R.string.border_stroke_selected),
                                    value = editPoint.borderStrokeSelected
                                        ?: defaultBorderStrokeSelected,
                                    valueRange = 0f..50f,
                                    color = MaterialTheme.colorScheme.primary,
                                    onReset = {
                                        editPoint =
                                            editPoint.copy(borderStrokeSelected = null)
                                    }
                                ) {
                                    editPoint = editPoint.copy(borderStrokeSelected = it)
                                }


                                ColorPickerRow(
                                    title = stringResource(R.string.border_color_selected),
                                    description = null,
                                    currentColor = editPoint.borderColorSelected?.let { Color(it) }
                                        ?: defaultBorderColorSelected
                                ) { selectedColor ->
                                    editPoint =
                                        editPoint.copy(borderColorSelected = selectedColor?.toArgb())
                                }


                                ColorPickerRow(
                                    title = stringResource(R.string.background_selected),
                                    description = null,
                                    currentColor = editPoint.backgroundColorSelected?.let { Color(it) }
                                        ?: defaultBackgroundColorSelected
                                ) { selectedColor ->
                                    editPoint = editPoint.copy(
                                        backgroundColorSelected = selectedColor.definedOrNull()
                                            ?.toArgb()
                                    )
                                }

                                ShapeRow(
                                    selected = editPoint.borderShapeSelected ?: defaultSwipePointsValues.borderShapeSelected!!,
                                    title = stringResource(R.string.edit_border_shape),
                                    onReset = {
                                        editPoint = editPoint.copy(borderShapeSelected = null)
                                    }
                                ) { showSelectedShapePickerDialog = true }
                            }
                        }
                    }
                }
            }

            // Can not edit the haptic feedback in default mode, has to go to nest settings to edit it circle by circle
            DragonColumnGroup {
                if (!isDefaultEditing) {
                    HapticFeedBackEditorButtonWithPlayTest(
                        customHapticFeedback = editPoint.haptic ?: defaultHapticFeedback(),
                        onClick = { showHapticFeedbackEditor = true },
                    )
                } else {
                    Text(stringResource(R.string.you_can_edit_haptic_feedback_on_nest_settings))
                }
            }
        }

        ValidateCancelButtons(
            onCancel = onDismiss
        ) {
            onConfirm(editPoint)
        }

    }

    if (showEditIconDialog) {
        PointIconEditor(
            point = editPoint,
            onDismiss = { showEditIconDialog = false }
        ) { newIcon ->

            val previewPoint = point.copy(customIcon = newIcon)

            iconsViewModel.reloadIcon(previewPoint)

            showEditIconDialog = false
            editPoint = editPoint.copy(customIcon = newIcon)
        }
    }
    if (showEditActionDialog) {
        AddPointDialog(
            onDismiss = { showEditActionDialog = false },
            onActionSelected = { selectedAction ->
                editPoint = editPoint.copy(action = selectedAction)
                showEditActionDialog = false
            }
        )
    }

    if (showHoldAndRunActionDialog) {
        AddPointDialog(
            onDismiss = { showHoldAndRunActionDialog = false },
            onActionSelected = { selectedAction ->
                editPoint = editPoint.copy(holdAndRunAction = selectedAction)
                showHoldAndRunActionDialog = false
            }
        )
    }

    if (showShapePickerDialog) {
        ShapePickerDialog(
            selected = editPoint.borderShape ?: defaultSwipePointsValues.borderShape!!,
            onDismiss = { showShapePickerDialog = false }
        ) {
            editPoint = editPoint.copy(borderShape = it)
        }
    }

    if (showSelectedShapePickerDialog) {
        ShapePickerDialog(
            selected = editPoint.borderShapeSelected ?: defaultSwipePointsValues.borderShapeSelected!!,
            onDismiss = { showSelectedShapePickerDialog = false }
        ) {
            editPoint = editPoint.copy(borderShapeSelected = it)
        }
    }


    if (showHapticFeedbackEditor) {
        HapticFeedbackEditor(
            initial = editPoint.haptic,
            onDismiss = { showHapticFeedbackEditor = false }
        ) { newHaptic ->
            editPoint = editPoint.copy(haptic = newHaptic)
            showHapticFeedbackEditor = false
        }
    }

    /*    Cycle Actions  action editor    */
    if (editingCycleStageActionIndex != null) {
        val idx = editingCycleStageActionIndex!!
        AddPointDialog(
            onDismiss = { editingCycleStageActionIndex = null },
            onActionSelected = { selectedAction ->
                val current = editPoint.cycleActions ?: emptyList()
                if (idx < current.size) {
                    val updated = current.toMutableList().also { it[idx] = it[idx].copy(action = selectedAction) }
                    editPoint = editPoint.copy(cycleActions = updated)
                }
                editingCycleStageActionIndex = null
            }
        )
    }

    if (editingCycleStageHapticIndex != null) {
        val idx = editingCycleStageHapticIndex!!
        val currentStages = editPoint.cycleActions ?: emptyList()
        HapticFeedbackEditor(
            initial = currentStages.getOrNull(idx)?.hapticFeedback,
            onDismiss = { editingCycleStageHapticIndex = null }
        ) { newHaptic ->
            if (idx < currentStages.size) {
                val updated = currentStages.toMutableList().also { it[idx] = it[idx].copy(hapticFeedback = newHaptic) }
                editPoint = editPoint.copy(cycleActions = updated)
            }
            editingCycleStageHapticIndex = null
        }
    }

    if (showLiveNestNestPicker) {
        NestManagementDialog(
            onDismissRequest = { showLiveNestNestPicker = false },
            title = stringResource(R.string.pick_a_nest),
            onSelect = { selectedNest ->
                editPoint = editPoint.copy(
                    liveNestTargetNestId = selectedNest.id,
                    liveNestPreviewDelayMs = editPoint.liveNestPreviewDelayMs
                        ?: defaultLiveNestDelay,
                    liveNestScale = editPoint.liveNestScale
                        ?: defaultLiveNestScale,
                    liveNestGraceDistancePx = editPoint.liveNestGraceDistancePx
                        ?: defaultLiveNestGraceDistance,
                    liveNestMainNestOpacityPercent = editPoint.liveNestMainNestOpacityPercent
                        ?: defaultLiveNestMainNestOpacityPercent
                )
                showLiveNestNestPicker = false
            }
        )
    }
}

//change settings home by default