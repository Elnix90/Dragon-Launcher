@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.CycleActionStage
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.emptyPoint
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.isNotDefault
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.enumsui.select.PointFeaturePanel
import org.elnix.dragonlauncher.enumsui.select.SelectedUnselectedViewMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.round
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.Icon
import org.elnix.dragonlauncher.ui.base.animation.rememberAnimatedIcon
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
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.components.rememberBottomSheetState
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointEditor(
    point: Point,
    defaultPoint: Point,
    isDefaultEditing: Boolean,
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(), // Only used to get live nest stuff
    onDismiss: (Point) -> Unit
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
        onDismissRequest = { onDismiss(editPoint) },
        sheetState = rememberBottomSheetState(true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DialogTitle(
                text = stringResource(if (!isDefaultEditing) R.string.edit_point else R.string.edit_default_point),
                resetEnabled = editPoint.isNotDefault
            ) {
                editPoint = Point(
                    offset = editPoint.offset,
                    nestId = editPoint.nestId,
                    action = editPoint.action,
                    id = editPoint.id
                )
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
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(1f)
                )
            }
        }

        Spacer(5.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .heightIn(max = 800.dp)
                .verticalScroll(rememberScrollState())
        ) {

            DragonSettingsGroup(R.string.special_options) {
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
                    @Suppress("UnusedExpression")
                    when (expandedFeature) {
                        PointFeaturePanel.LiveNest -> {

                            val liveNestTargetNestId = editPoint.liveNestTargetNestId
                            val liveNestEnabled = liveNestTargetNestId != null
                            val targetNest = if (liveNestEnabled) pointsViewModel.pointsService.findNestById(liveNestTargetNestId) else null
                            val nestLabel = targetNest?.name ?: targetNest?.let { "Nest ${it.id}" } ?: ""

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                if (!liveNestEnabled && !isDefaultEditing) {
                                    DragonButton(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        onClick = { showLiveNestNestPicker = true }
                                    ) {
                                        Text(stringResource(R.string.live_nest_pick_nest))
                                    }

                                } else {
                                    if (isDefaultEditing) {
                                        TextWithDescription(
                                            text = stringResource(R.string.default_point_live_nest_defaults),
                                            description = stringResource(R.string.default_point_live_nest_defaults_summary),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxWidth()
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
                                                icon = R.drawable.close,
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
                                    ) {
                                        editPoint = editPoint.copy(liveNestPreviewDelayMs = it.takeIf {
                                            it != emptyPoint.getLiveNestPreviewDelayMs(
                                                defaultPoint,
                                                isDefaultEditing
                                            )
                                        })
                                    }

                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_scale),
                                        value = editPoint.getLiveNestScale(defaultPoint, isDefaultEditing),
                                        valueRange = 0.3f..1.0f,
                                        resetEnabled = editPoint.liveNestScale != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestScale = null)
                                        }
                                    ) {
                                        editPoint = editPoint.copy(liveNestScale = it.takeIf {
                                            it != emptyPoint.getLiveNestScale(
                                                defaultPoint,
                                                isDefaultEditing
                                            )
                                        })
                                    }

                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_grace_distance),
                                        value = editPoint.getLiveNestGraceDistance(defaultPoint, isDefaultEditing),
                                        valueRange = (-1).dp..3000.dp,
                                        resetEnabled = editPoint.liveNestGraceDistance != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestGraceDistance = null)
                                        }
                                    ) {
                                        editPoint = editPoint.copy(liveNestGraceDistance = it.takeIf {
                                            it != emptyPoint.getLiveNestGraceDistance(
                                                defaultPoint,
                                                isDefaultEditing
                                            )
                                        })
                                    }

                                    SwitchRow(
                                        title = stringResource(R.string.fast_activation),
                                        description = stringResource(R.string.fast_activation_desc),
                                        state = editPoint.getFastActivation(defaultPoint, isDefaultEditing),
                                        resetEnabled = editPoint.fastActivation != null,
                                        onReset = {
                                            editPoint = editPoint.copy(fastActivation = null)
                                        }
                                    ) {
                                        editPoint = editPoint.copy(fastActivation = it.takeIf {
                                            it != emptyPoint.getFastActivation(
                                                defaultPoint,
                                                isDefaultEditing
                                            )
                                        })
                                    }


                                    SwitchRow(
                                        state = editPoint.getLiveNestSnapsToFingerPosition(defaultPoint, isDefaultEditing),
                                        title = stringResource(R.string.live_nest_snaps_to_finger_position),
                                        description = stringResource(R.string.live_nest_snaps_to_finger_position_desc),
                                        resetEnabled = editPoint.liveNestSnapsToFingerPosition != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestSnapsToFingerPosition = null)
                                        }
                                    ) { on ->
                                        editPoint = editPoint.copy(liveNestSnapsToFingerPosition = on.takeIf {
                                            it != emptyPoint.getLiveNestSnapsToFingerPosition(
                                                defaultPoint,
                                                isDefaultEditing
                                            )
                                        })
                                    }

                                    SliderWithLabel(
                                        label = stringResource(R.string.live_nest_sub_nest_opacity),
                                        description = stringResource(R.string.live_nest_dim_main_nest_desc),
                                        value = editPoint.getLiveNestMainNestOpacityPercent(defaultPoint, isDefaultEditing),
                                        valueRange = 0..100,
                                        resetEnabled = editPoint.liveNestSubNestOpacityPercent != null,
                                        onReset = {
                                            editPoint = editPoint.copy(liveNestSubNestOpacityPercent = null)
                                        }
                                    ) { newValue ->
                                        editPoint = editPoint.copy(liveNestSubNestOpacityPercent = newValue.takeIf {
                                            it != emptyPoint.getLiveNestMainNestOpacityPercent(
                                                defaultPoint,
                                                isDefaultEditing
                                            )
                                        })
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

                                        Card(
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxWidth(),
                                            shape = MaterialTheme.shapes.large
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
                                                        contentDescription = stringResource(R.string.disable),
                                                        colors = AppObjectsColors.cancelIconButtonColors()
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

                                                DragonButton(
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

                                    DragonButton(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
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

                                AnimatedVisibility(cycleStages.isNotEmpty() || isDefaultEditing) {
                                    val cycleActionLoop = editPoint.getCycleActionsStageLoop(defaultPoint, isDefaultEditing)
                                    SwitchRow(
                                        state = cycleActionLoop,
                                        title = stringResource(R.string.cycle_actions_loop),
                                        description = stringResource(R.string.cycle_actions_loop_desc)
                                    ) { on ->
                                        editPoint = editPoint.copy(
                                            cycleActionsLoop = on.takeIf { it != emptyPoint.getCycleActionsStageLoop(defaultPoint, isDefaultEditing) }
                                        )
                                    }

                                    AnimatedVisibility(cycleActionLoop) {
                                        SliderWithLabel(
                                            label = stringResource(R.string.cycle_actions_loop_delay),
                                            value = editPoint.getCycleActionsStageLoopDelayMs(defaultPoint, isDefaultEditing),
                                            valueRange = 50..5000,
                                            resetEnabled = true,
                                            onReset = {
                                                editPoint = editPoint.copy(cycleActionsLoopDelayMs = null)
                                            }
                                        ) { ms ->
                                            editPoint = editPoint.copy(cycleActionsLoopDelayMs = ms.takeIf {
                                                it != emptyPoint.getCycleActionsStageLoopDelayMs(
                                                    defaultPoint,
                                                    isDefaultEditing
                                                )
                                            })
                                        }
                                    }
                                }
                            }
                        }

                        PointFeaturePanel.HoldAndRun -> {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                if (isDefaultEditing) {
                                    SliderWithLabel(
                                        label = stringResource(R.string.default_hold_and_run_delay),
                                        description = stringResource(R.string.default_hold_and_run_delay_summary),
                                        value = editPoint.getHoldAndRunDelayMs(defaultPoint, true),
                                        valueRange = 100..5000,
                                        resetEnabled = editPoint.holdAndRunDelayMs != null,
                                        onReset = {
                                            editPoint = editPoint.copy(holdAndRunDelayMs = null)
                                        }
                                    ) {
                                        editPoint = editPoint.copy(holdAndRunDelayMs = it.takeIf {
                                            it != emptyPoint.getHoldAndRunDelayMs(
                                                defaultPoint,
                                                true
                                            )
                                        })
                                    }

                                } else {
                                    val harEnabled = editPoint.holdAndRunDelayMs != null

                                    if (!harEnabled) {
                                        DragonButton(
                                            onClick = {
                                                editPoint = editPoint.copy(
                                                    holdAndRunDelayMs = defaultPoint.holdAndRunDelayMs ?: 1000
                                                )
                                            },
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(stringResource(R.string.hold_and_run_enable))
                                        }
                                    } else {
                                        SliderWithLabel(
                                            label = stringResource(R.string.hold_and_run_delay),
                                            value = editPoint.getHoldAndRunDelayMs(defaultPoint, false),
                                            valueRange = 50..5000,
                                            resetEnabled = editPoint.holdAndRunDelayMs != null,
                                            onReset = {
                                                editPoint = editPoint.copy(holdAndRunDelayMs = null)
                                            }
                                        ) { newDelay ->
                                            editPoint = editPoint.copy(holdAndRunDelayMs = newDelay.takeIf {
                                                it != emptyPoint.getHoldAndRunDelayMs(
                                                    defaultPoint,
                                                    false
                                                )
                                            })
                                        }

                                        SwitchRow(
                                            state = editPoint.holdAndRunAction != null,
                                            title = stringResource(R.string.hold_and_run_custom_action),
                                            description = stringResource(R.string.hold_and_run_custom_action_desc)
                                        ) { on ->
                                            editPoint = if (on) {
                                                showHoldAndRunActionDialog = true
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
                                                    .padding(10.dp)
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

                                        DragonButton(
                                            onClick = {
                                                editPoint = editPoint.copy(
                                                    holdAndRunDelayMs = null,
                                                    holdAndRunAction = null
                                                )
                                            },
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxWidth(),
                                            colors = AppObjectsColors.cancelButtonColors()
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

                        null -> null
                    }
                }
            }


            if (!isDefaultEditing) {
                DragonSettingsGroup(R.string.name_and_action) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        DragonButton(
                            onClick = { showEditActionDialog = true },
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
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
                    }


                    val focusManager = LocalFocusManager.current
                    val animatedIcon = rememberAnimatedIcon()

                    TextField(
                        value = editPoint.customName ?: "",
                        onValueChange = {
                            editPoint = editPoint.copy(customName = it.takeIf { it.isNotEmpty() })
                        },
                        placeholder = { Text(stringResource(R.string.custom_name)) },
                        colors = AppObjectsColors.outlinedTextFieldColors(
                            removeBorder = true
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                animatedIcon.setSuccess()
                            }
                        ),
                        trailingIcon = {
                            animatedIcon.Icon(
                                defaultIcon = R.drawable.reset,
                                enabled = editPoint.customName?.isNotEmpty() == true
                            ) {
                                focusManager.clearFocus()
                                animatedIcon.setSuccess()
                            }
                        }
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

            DragonSettingsGroup(R.string.size) {
                SliderWithLabel(
                    label = stringResource(R.string.inner_padding),
                    value = editPoint.getInnerPadding(defaultPoint, isDefaultEditing),
                    valueRange = 0.dp..100.dp,
                    resetEnabled = editPoint.innerPadding != null,
                    onReset = { editPoint = editPoint.copy(innerPadding = null) }
                ) {
                    editPoint = editPoint.copy(
                        innerPadding = it.takeIf {
                            it.value.round(2) != emptyPoint.getInnerPadding(
                                defaultPoint,
                                isDefaultEditing
                            ).value.round(2)
                        }
                    )
                }

                SliderWithLabel(
                    label = stringResource(R.string.size),
                    value = editPoint.getSize(defaultPoint, isDefaultEditing),
                    valueRange = 1.dp..200.dp,
                    resetEnabled = editPoint.size != null,
                    onReset = { editPoint = editPoint.copy(size = null) }
                ) {
                    editPoint = editPoint.copy(
                        size = it.takeIf {
                            it.value.round(2) != emptyPoint.getSize(
                                defaultPoint,
                                isDefaultEditing
                            ).value.round(2)
                        }
                    )
                }
            }


            DragonSettingsGroup(R.string.appearance) {
                if (!isDefaultEditing) {
                    DragonButton(
                        onClick = { showEditIconDialog = true },
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.edit_icon))
                        Spacer(5.dp)
                        Icon(
                            painter = painterResource(R.drawable.edit_rounded),
                            contentDescription = stringResource(R.string.edit_icon)
                        )
                    }
                }


                // Selected / Unselected Options Toggler
                var selectedView by remember { mutableStateOf(SelectedUnselectedViewMode.Selected) }

                SingleSelectConnectedButtonRow(
                    entries = SelectedUnselectedViewMode.entries,
                    checked = { selectedView == it },
                ) { selectedView = it }


                AnimatedContent(targetState = selectedView) { view ->
                    Column {
                        val selected = when (view) {
                            SelectedUnselectedViewMode.Unselected -> false
                            SelectedUnselectedViewMode.Selected -> true
                        }

                        if (selected) {
                            SliderWithLabel(
                                label = stringResource(R.string.border_stroke_selected),
                                value = editPoint.getBorderStroke(true, defaultPoint, isDefaultEditing),
                                valueRange = 0.dp..50.dp,
                                resetEnabled = editPoint.borderStrokeSelected != null,
                                onReset = {
                                    editPoint = editPoint.copy(borderStrokeSelected = null)
                                }
                            ) { newValue ->
                                editPoint = editPoint.copy(
                                    borderStrokeSelected = newValue.takeIf {
                                        it.value.round(2) != emptyPoint.getBorderStroke(
                                            true,
                                            defaultPoint,
                                            isDefaultEditing
                                        ).value.round(2)
                                    }
                                )
                            }

                            ColorPickerRow(
                                title = stringResource(R.string.border_color_selected),
                                description = null,
                                currentColor = editPoint.getBorderColor(true, defaultPoint, extraColors, isDefaultEditing)
                            ) { selectedColor ->
                                editPoint = editPoint.copy(
                                    borderColorSelected = selectedColor.takeIf {
                                        it != emptyPoint.getBorderColor(
                                            true,
                                            defaultPoint,
                                            extraColors,
                                            isDefaultEditing
                                        )
                                    }
                                )
                            }

                            ColorPickerRow(
                                title = stringResource(R.string.background_selected),
                                description = null,
                                currentColor = editPoint.getBackgroundColor(true, defaultPoint, extraColors, isDefaultEditing)
                            ) { selectedColor ->
                                editPoint = editPoint.copy(
                                    backgroundColorSelected = selectedColor.takeIf {
                                        it != emptyPoint.getBackgroundColor(
                                            true,
                                            defaultPoint,
                                            extraColors,
                                            isDefaultEditing
                                        )
                                    })
                            }

                            ShapeRow(
                                selected = editPoint.getBorderShape(true, defaultPoint, isDefaultEditing),
                                title = stringResource(R.string.edit_border_shape),
                                resetEnabled = editPoint.borderShapeSelected != null,
                                onReset = {
                                    editPoint = editPoint.copy(borderShapeSelected = null)
                                }
                            ) { showShapePickerDialog = true }

                        } else {
                            SliderWithLabel(
                                label = stringResource(R.string.border_stroke),
                                value = editPoint.getBorderStroke(false, defaultPoint, isDefaultEditing),
                                valueRange = 0.dp..50.dp,
                                resetEnabled = editPoint.borderStroke != null,
                                onReset = {
                                    editPoint = editPoint.copy(borderStroke = null)
                                }
                            ) { newValue ->
                                editPoint = editPoint.copy(
                                    borderStroke = newValue.takeIf {
                                        it.value.round(2) != emptyPoint.getBorderStroke(
                                            false,
                                            defaultPoint,
                                            isDefaultEditing
                                        ).value.round(2)
                                    }
                                )
                            }

                            ColorPickerRow(
                                title = stringResource(R.string.border_color),
                                description = null,
                                currentColor = editPoint.getBorderColor(false, defaultPoint, extraColors, isDefaultEditing)
                            ) { selectedColor ->
                                editPoint = editPoint.copy(
                                    borderColor = selectedColor.takeIf {
                                        it != emptyPoint.getBorderColor(
                                            false,
                                            defaultPoint,
                                            extraColors,
                                            isDefaultEditing
                                        )
                                    }
                                )
                            }

                            ColorPickerRow(
                                title = stringResource(R.string.background_color),
                                description = null,
                                currentColor = editPoint.getBackgroundColor(false, defaultPoint, extraColors, isDefaultEditing)
                            ) { selectedColor ->
                                editPoint = editPoint.copy(
                                    backgroundColor = selectedColor.takeIf {
                                        it != emptyPoint.getBackgroundColor(
                                            false,
                                            defaultPoint,
                                            extraColors,
                                            isDefaultEditing
                                        )
                                    }
                                )
                            }

                            ShapeRow(
                                selected = editPoint.getBorderShape(false, defaultPoint, isDefaultEditing),
                                title = stringResource(R.string.edit_border_shape),
                                resetEnabled = editPoint.borderShape != null,
                                onReset = {
                                    editPoint = editPoint.copy(borderShape = null)
                                }
                            ) { showShapePickerDialog = true }
                        }
                    }
                }
            }

            if (!isDefaultEditing) {
                HapticFeedBackEditorButtonWithPlayTest(
                    customHapticFeedback = editPoint.haptic ?: defaultHapticFeedback(),
                    onClick = { showHapticFeedbackEditor = true },
                )
            }
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
        ) { newShape ->
            editPoint = editPoint.copy(
                borderShape = newShape.takeIf {
                    it != emptyPoint.getBorderShape(
                        false,
                        defaultPoint,
                        isDefaultEditing
                    )
                }
            )
        }
    }

    if (showSelectedShapePickerDialog) {
        ShapePickerDialog(
            selected = editPoint.borderShapeSelected ?: Point.defaultBorderShapeSelected,
            onDismiss = { showSelectedShapePickerDialog = false }
        ) { newShape ->
            editPoint = editPoint.copy(
                borderShapeSelected = newShape.takeIf {
                    it != emptyPoint.getBorderShape(
                        true,
                        defaultPoint,
                        isDefaultEditing
                    )
                }
            )
        }
    }


    val defaultHaptic = emptyPoint.getHaptic(defaultPoint, isDefaultEditing)
    if (showHapticFeedbackEditor) {
        HapticFeedbackEditor(
            initial = editPoint.haptic,
            default = defaultHaptic
        ) { newHaptic ->
            editPoint = editPoint.copy(haptic = newHaptic.takeIf { it != defaultHaptic })
            showHapticFeedbackEditor = false
        }
    }

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
            default = defaultHaptic
        ) { newHaptic ->
            if (idx < currentStages.size) {
                val updated = currentStages.toMutableList()
                    .also { list -> list[idx] = list[idx].copy(hapticFeedback = newHaptic.takeIf { it != defaultHaptic }) }
                editPoint = editPoint.copy(cycleActions = updated)
            }
            editingCycleStageHapticIndex = null
        }
    }

    if (showLiveNestNestPicker) {
        NestManagementDialog(
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
        ) { showLiveNestNestPicker = false }
    }
}
