@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs.editors

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.CycleActionStage
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.defaultLiveNestMainNestOpacityPercent
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.enumsui.select.PointFeaturePanel
import org.elnix.dragonlauncher.enumsui.select.SelectedUnselectedViewMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.specifiedOrNull
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.PointPreviewCanvas
import org.elnix.dragonlauncher.ui.defaultHapticFeedback
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedBackEditorButtonWithPlayTest
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedbackEditor
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.components.rememberBottomSheetState
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun EditPointSheet(
    point: Point,
    defaultPoint: Point,
    isDefaultEditing: Boolean = false,
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(), // Only use to get live nest stuff
    onDismiss: () -> Unit,
    onConfirm: (Point) -> Unit
) {
    val extraColors = LocalExtraColors.current

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
    val actionColor = editPoint.action.actionColor(extraColors, editPoint.customActionColor)

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
        sheetState = rememberBottomSheetState(true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DialogTitle(stringResource(if (!isDefaultEditing) R.string.edit_point else R.string.edit_default_point))
                ResetIcon {
                    editPoint = Point(
                        offset = editPoint.offset,
                        nestId = editPoint.nestId,
                        action = editPoint.action,
                        id = editPoint.id
                    )
                }
            }

            DragonColumnGroup {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(20.dp),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    Text(
//                        text = stringResource(R.string.unselected_action),
//                        color = MaterialTheme.colorScheme.onSurface,
//                        style = MaterialTheme.typography.labelSmall
//                    )
//
//                    Text(
//                        text = stringResource(R.string.selected_action),
//                        color = MaterialTheme.colorScheme.onSurface,
//                        style = MaterialTheme.typography.labelSmall
//                    )
//                }

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

                            val liveNestTargetNestId = editPoint.liveNestTargetNestId
                            val liveNestEnabled = liveNestTargetNestId != null
                            val targetNest = if (liveNestEnabled) pointsViewModel.pointsService.findNestById(liveNestTargetNestId) else null
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
                                                    liveNestGraceDistance = null,
                                                    liveNestSubNestOpacityPercent = null
                                                )
                                            }
                                        }
                                    }


                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_hold_delay),
                                        value = editPoint.getLiveNestPreviewDelayMs(defaultPoint, isDefaultEditing),
                                        valueRange = 0..5000,
                                        resetEnabled = editPoint.liveNestPreviewDelayMs != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestPreviewDelayMs = null)
                                        }
                                    ) { editPoint = editPoint.copy(liveNestPreviewDelayMs = it) }

                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_scale),
                                        value = editPoint.getLiveNestScale(defaultPoint, isDefaultEditing),
                                        valueRange = 0.3f..1.0f,
                                        resetEnabled = editPoint.liveNestScale != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestScale = null)
                                        }
                                    ) { editPoint = editPoint.copy(liveNestScale = it) }

                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_grace_distance),
                                        value = editPoint.getLiveNestGraceDistance(defaultPoint, isDefaultEditing),
                                        valueRange = (-1).dp..3000.dp,
                                        resetEnabled = editPoint.liveNestGraceDistance != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestGraceDistance = null)
                                        }
                                    ) { editPoint = editPoint.copy(liveNestGraceDistance = it) }

                                    SwitchRow(
                                        state = editPoint.getLiveNestSnapsToFingerPosition(defaultPoint, isDefaultEditing),
                                        title = stringResource(R.string.live_nest_snaps_to_finger_position),
                                        description = stringResource(R.string.live_nest_snaps_to_finger_position_desc),
                                        resetEnabled = editPoint.liveNestSnapsToFingerPosition != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestSnapsToFingerPosition = null)
                                        }
                                    ) { on ->
                                        editPoint = if (!on) {
                                            editPoint.copy(liveNestSnapsToFingerPosition = false)
                                        } else {
                                            editPoint.copy(liveNestSnapsToFingerPosition = null)
                                        }
                                    }

                                    val currentLIveNestOpacityPercent = (editPoint.getLiveNestMainNestOpacityPercent(defaultPoint, isDefaultEditing))
                                    SwitchRow(
                                        state = currentLIveNestOpacityPercent != -1,
                                        title = stringResource(R.string.live_nest_dim_main_nest),
                                        description = stringResource(R.string.live_nest_dim_main_nest_desc)
                                    ) { on ->
                                        editPoint = if (on) {
                                            editPoint.copy(liveNestSubNestOpacityPercent = defaultLiveNestMainNestOpacityPercent)
                                        } else {
                                            editPoint.copy(liveNestSubNestOpacityPercent = -1)
                                        }
                                    }

                                    AnimatedVisibility(currentLIveNestOpacityPercent != -1) {
                                        SliderWithLabel(
                                            label = stringResource(R.string.live_nest_main_nest_opacity),
                                            value = currentLIveNestOpacityPercent,
                                            valueRange = 0..100,
                                            resetEnabled = editPoint.liveNestSubNestOpacityPercent != null,
                                            onReset = {
                                                editPoint = editPoint.copy(liveNestSubNestOpacityPercent = null)
                                            }
                                        ) { editPoint = editPoint.copy(liveNestSubNestOpacityPercent = it) }
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
                                            shape = MaterialTheme.shapes.large,
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
                                                    resetEnabled = stage.triggerTimeMs != (defaultPoint.cycleActionsLoopDelayMs
                                                        ?: Point.defaultCycleActionsLoopDelayMs),
                                                    onReset = {
                                                        val updated = cycleStages.toMutableList().also {
                                                            it[index] = it[index].copy(
                                                                triggerTimeMs = defaultPoint.cycleActionsLoopDelayMs
                                                                    ?: Point.defaultCycleActionsLoopDelayMs
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
                                        value = editPoint.getCycleActionsStageLoopDelayMs(defaultPoint, true),
                                        valueRange = 50..5000,
                                        resetEnabled = editPoint.cycleActionsLoopDelayMs != null,
                                        onReset = {
                                            editPoint = editPoint.copy(cycleActionsLoopDelayMs = null)
                                        }
                                    ) { ms ->
                                        editPoint = editPoint.copy(cycleActionsLoopDelayMs = ms)
                                    }
                                }

                                val currentLoopDelay = editPoint.getCycleActionsStageLoopDelayMs(defaultPoint, isDefaultEditing)
                                /*   Loop (optional tail before cycle restarts)   */
                                if (cycleStages.isNotEmpty() || isDefaultEditing) {
                                    SwitchRow(
                                        state = currentLoopDelay != -1,
                                        title = stringResource(R.string.cycle_actions_loop),
                                        description = stringResource(R.string.cycle_actions_loop_desc)
                                    ) { on ->
                                        editPoint = editPoint.copy(
                                            cycleActionsLoopDelayMs =
                                                if (on) defaultPoint.cycleActionsLoopDelayMs ?: Point.defaultCycleActionsLoopDelayMs else -1
                                        )
                                    }

                                    AnimatedVisibility(currentLoopDelay != -1) {
                                        SliderWithLabel(
                                            label = stringResource(R.string.cycle_actions_loop_delay),
                                            value = currentLoopDelay,
                                            valueRange = 50..5000,
                                            resetEnabled = true,
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
                                    Text(
                                        text = stringResource(R.string.default_hold_and_run_delay_summary),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    SliderWithLabel(
                                        label = stringResource(R.string.default_hold_and_run_delay),
                                        value = editPoint.getHoldAndRunDelayMs(defaultPoint, true),
                                        valueRange = 100..5000,
                                        resetEnabled = editPoint.holdAndRunDelayMs != null,
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
                                            value = editPoint.getHoldAndRunDelayMs(defaultPoint, false),
                                            valueRange = 100..5000,
                                            resetEnabled = editPoint.holdAndRunDelayMs != null,
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
                                            val harColor = harAction.actionColor(extraColors, editPoint.customActionColor)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.large)
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
                                ResetIcon { editPoint = editPoint.copy(customName = null) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppObjectsColors.outlinedTextFieldColors()
                    )

                    ColorPickerRow(
                        title = stringResource(R.string.custom_action_color),
                        description = null,
                        currentColor = editPoint.customActionColor ?: currentActionColor,
                    ) { selectedColor ->
                        editPoint = editPoint.copy(customActionColor = selectedColor)
                    }
                }
            }

            DragonColumnGroup {
                SliderWithLabel(
                    label = stringResource(R.string.inner_padding),
                    value = editPoint.getInnerPadding(defaultPoint, isDefaultEditing),
                    valueRange = 0.dp..100.dp,
                    resetEnabled = editPoint.innerPadding != null,
                    onReset = { editPoint = editPoint.copy(innerPadding = null) }
                ) { editPoint = editPoint.copy(innerPadding = it) }

                SliderWithLabel(
                    label = stringResource(R.string.size),
                    value = editPoint.getSize(defaultPoint, isDefaultEditing),
                    valueRange = 1.dp..200.dp,
                    resetEnabled = defaultPoint.size != null,
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
                        val selected = when (view) {

                            SelectedUnselectedViewMode.Unselected -> false
                            SelectedUnselectedViewMode.Selected -> true
                        }

                        SliderWithLabel(
                            label = stringResource(if (selected) R.string.border_stroke else R.string.border_stroke_selected),
                            value = editPoint.getBorderStroke(selected, defaultPoint, isDefaultEditing),
                            valueRange = 0.dp..50.dp,
                            resetEnabled = editPoint.borderStroke != null,
                            onReset = {
                                editPoint = editPoint.copy(borderStroke = null)
                            }
                        ) {
                            editPoint = editPoint.copy(borderStroke = it)
                        }

                        ColorPickerRow(
                            title = stringResource(if (selected) R.string.border_color else R.string.border_color_selected),
                            description = null,
                            currentColor = editPoint.getBorderColor(selected, defaultPoint, extraColors, isDefaultEditing)
                        ) { selectedColor ->
                            editPoint = editPoint.copy(borderColor = selectedColor)
                        }

                        ColorPickerRow(
                            title = stringResource(if (selected) R.string.background_color else R.string.background_selected),
                            description = null,
                            currentColor = editPoint.getBackgroundColor(selected, defaultPoint, extraColors, isDefaultEditing)
                        ) { selectedColor ->
                            editPoint = editPoint.copy(
                                backgroundColor = selectedColor.specifiedOrNull()
                            )
                        }

                        ShapeRow(
                            selected = editPoint.getBorderShape(selected, defaultPoint, isDefaultEditing),
                            title = stringResource(R.string.edit_border_shape),
                            resetEnabled = editPoint.borderShape != null,
                            onReset = {
                                editPoint = editPoint.copy(borderShape = null)
                            }
                        ) { showShapePickerDialog = true }
                    }
                }
            }

            // Can not edit the haptic feedback in default mode, has to go to nest settings to edit it circle by circle
            DragonSettingsGroup(R.string.haptic_feedback) {
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
            selected = editPoint.borderShape ?: Point.defaultBorderShape,
            onDismiss = { showShapePickerDialog = false }
        ) {
            editPoint = editPoint.copy(borderShape = it)
        }
    }

    if (showSelectedShapePickerDialog) {
        ShapePickerDialog(
            selected = editPoint.borderShapeSelected ?: Point.defaultBorderShapeSelected,
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
                    liveNestPreviewDelayMs = editPoint.getLiveNestPreviewDelayMs(defaultPoint, isDefaultEditing),
                    liveNestScale = editPoint.getLiveNestScale(defaultPoint, isDefaultEditing),
                    liveNestGraceDistance = editPoint.getLiveNestGraceDistance(defaultPoint, isDefaultEditing),
                    liveNestSubNestOpacityPercent = editPoint.getLiveNestMainNestOpacityPercent(defaultPoint, isDefaultEditing)
                )
                showLiveNestNestPicker = false
            }
        )
    }
}
