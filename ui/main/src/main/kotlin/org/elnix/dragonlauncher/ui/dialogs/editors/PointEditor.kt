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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.elnix.dragonlauncher.base.model.enumsui.select.PointFeaturePanel
import org.elnix.dragonlauncher.base.model.enumsui.select.SelectedUnselectedViewMode
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.CycleActionStage
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.emptyPoint
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.isNotDefault
import org.elnix.dragonlauncher.base.model.serializables.isSpecified
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.round
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.PointPreviewCanvas
import org.elnix.dragonlauncher.ui.defaultHapticFeedback
import org.elnix.dragonlauncher.ui.dialogs.ActionPickerDialog
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedBackEditorButtonWithPlayTest
import org.elnix.dragonlauncher.ui.dialogs.HapticFeedbackEditor
import org.elnix.dragonlauncher.ui.dialogs.NestManagementDialog
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.TextRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.ShapeRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointEditor(
    point: Point,
    defaultPoint: Point,
    isDefaultEditing: Boolean,
    pointsViewModel: PointsViewModel = activityViewModel(), // Only used to get live nest stuff
    onDismiss: (Point) -> Unit
) {
    val extraColors = LocalExtraColors.current

    var editPoint by remember { mutableStateOf(point) }
    var showEditIconDialog by remember { mutableStateOf(false) }
    var showEditActionDialog by remember { mutableStateOf(false) }
    var showShapePickerDialog by remember { mutableStateOf(false) }
    var showShapeSelectedPickerDialog by remember { mutableStateOf(false) }
    var showSelectedShapePickerDialog by remember { mutableStateOf(false) }
    var showHapticFeedbackEditor by remember { mutableStateOf(false) }

    var expandedFeaturePanel: PointFeaturePanel? by remember { mutableStateOf(null) }
    var showLiveNestNestPicker by remember { mutableStateOf(false) }
    var showHoldAndRunActionDialog by remember { mutableStateOf(false) }

    var editingCycleStageActionIndex by remember { mutableStateOf<Int?>(null) }
    var editingCycleStageHapticIndex by remember { mutableStateOf<Int?>(null) }

    var selectedView by remember { mutableStateOf(SelectedUnselectedViewMode.Unselected) }

    val label = editPoint.customName ?: actionLabel(editPoint.action)
    val actionColor = editPoint.action.actionColor(extraColors, editPoint.customActionColor)

    DragonModalBottomSheet(
        onDismissRequest = { onDismiss(editPoint) },
        skipPartiallyExpanded = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DialogTitle(
                text = stringResource(if (!isDefaultEditing) R.string.edit_point else R.string.edit_default_point),
                resetEnabled = editPoint.isNotDefault
            ) {
                editPoint =
                    Point(
                        offset = editPoint.offset,
                        nestId = editPoint.nestId,
                        action = editPoint.action,
                        id = editPoint.id
                    )
            }

            DragonSettingsGroup {
                PointPreviewCanvas(
                    editPoint = editPoint,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    isDefaultEditing = isDefaultEditing,
                    modifier = Modifier.fillMaxWidth(1f)
                ) {
                    selectedView = it
                }
            }
        }

        this.Spacer(5.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
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
                    },
                    modifier = Modifier.dragonSettingGroup()
                ) {
                    expandedFeaturePanel =
                        it.takeIf {
                            expandedFeaturePanel != it
                        }
                }

                @Suppress("UnusedExpression")
                when (expandedFeaturePanel) {
                    PointFeaturePanel.LiveNest -> {
                        val liveNestTargetNestId = editPoint.liveNestTargetNestId
                        val liveNestEnabled = liveNestTargetNestId != null
                        val targetNest = if (liveNestEnabled) pointsViewModel.pointsService.findNestById(liveNestTargetNestId) else null
                        val nestLabel = targetNest?.name ?: targetNest?.let { "Nest ${it.id}" } ?: ""

                        if (!liveNestEnabled && !isDefaultEditing) {
                            DragonButton(
                                modifier =
                                    Modifier
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
                                    modifier = Modifier.dragonSettingGroup()
                                ) {
                                    DragonButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = { showLiveNestNestPicker = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.edit_rounded),
                                            contentDescription = null
                                        )
                                        this.Spacer(5.dp)
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
                                        contentDescription = R.string.disable
                                    ) {
                                        editPoint =
                                            editPoint.copy(
                                                liveNestTargetNestId = null,
                                                liveNestPreviewDelayMs = null,
                                                liveNestScale = null,
                                                liveNestGraceDistance = null,
                                                liveNestSubNestOpacityPercent = null
                                            )
                                    }
                                }
                            }

                            this.SliderWithLabel(
                                label = stringResource(R.string.live_nest_hold_delay),
                                value = editPoint.getLiveNestPreviewDelayMs(defaultPoint, isDefaultEditing),
                                valueRange = 0..5000,
                                resetEnabled = editPoint.liveNestPreviewDelayMs != null,
                                onReset = {
                                    editPoint = editPoint.copy(liveNestPreviewDelayMs = null)
                                }
                            ) {
                                editPoint =
                                    editPoint.copy(
                                        liveNestPreviewDelayMs =
                                            it.takeIf {
                                                it !=
                                                    emptyPoint.getLiveNestPreviewDelayMs(
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    )
                                            }
                                    )
                            }

                            this.SliderWithLabel(
                                label = stringResource(R.string.live_nest_scale),
                                value = editPoint.getLiveNestScale(defaultPoint, isDefaultEditing),
                                valueRange = 0.3f..1.0f,
                                resetEnabled = editPoint.liveNestScale != null,
                                onReset = {
                                    editPoint = editPoint.copy(liveNestScale = null)
                                }
                            ) {
                                editPoint =
                                    editPoint.copy(
                                        liveNestScale =
                                            it.takeIf {
                                                it !=
                                                    emptyPoint.getLiveNestScale(
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    )
                                            }
                                    )
                            }

                            this.SliderWithLabel(
                                label = stringResource(R.string.live_nest_grace_distance),
                                value = editPoint.getLiveNestGraceDistance(defaultPoint, isDefaultEditing),
                                valueRange = (-1).dp..3000.dp,
                                resetEnabled = editPoint.liveNestGraceDistance != null,
                                onReset = {
                                    editPoint = editPoint.copy(liveNestGraceDistance = null)
                                }
                            ) {
                                editPoint =
                                    editPoint.copy(
                                        liveNestGraceDistance =
                                            it.takeIf {
                                                it !=
                                                    emptyPoint.getLiveNestGraceDistance(
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    )
                                            }
                                    )
                            }

                            this.SwitchRow(
                                title = R.string.fast_activation,
                                description = R.string.fast_activation_desc,
                                state = editPoint.getFastActivation(defaultPoint, isDefaultEditing),
                                resetEnabled = editPoint.fastActivation != null,
                                onReset = {
                                    editPoint = editPoint.copy(fastActivation = null)
                                }
                            ) {
                                editPoint =
                                    editPoint.copy(
                                        fastActivation =
                                            it.takeIf {
                                                it !=
                                                    emptyPoint.getFastActivation(
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    )
                                            }
                                    )
                            }

                            this.SwitchRow(
                                state = editPoint.getLiveNestSnapsToFingerPosition(defaultPoint, isDefaultEditing),
                                title = R.string.live_nest_snaps_to_finger_position,
                                description = R.string.live_nest_snaps_to_finger_position_desc,
                                resetEnabled = editPoint.liveNestSnapsToFingerPosition != null,
                                onReset = {
                                    editPoint = editPoint.copy(liveNestSnapsToFingerPosition = null)
                                }
                            ) { on ->
                                editPoint =
                                    editPoint.copy(
                                        liveNestSnapsToFingerPosition =
                                            on.takeIf {
                                                it !=
                                                    emptyPoint.getLiveNestSnapsToFingerPosition(
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    )
                                            }
                                    )
                            }

                            this.SliderWithLabel(
                                label = stringResource(R.string.live_nest_sub_nest_opacity),
                                description = stringResource(R.string.live_nest_dim_main_nest_desc),
                                value = editPoint.getLiveNestMainNestOpacityPercent(defaultPoint, isDefaultEditing),
                                valueRange = 0..100,
                                resetEnabled = editPoint.liveNestSubNestOpacityPercent != null,
                                onReset = {
                                    editPoint = editPoint.copy(liveNestSubNestOpacityPercent = null)
                                }
                            ) { newValue ->
                                editPoint =
                                    editPoint.copy(
                                        liveNestSubNestOpacityPercent =
                                            newValue.takeIf {
                                                it !=
                                                    emptyPoint.getLiveNestMainNestOpacityPercent(
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    )
                                            }
                                    )
                            }
                        }
                    }

                    PointFeaturePanel.CycleActions -> {
                        val cycleStages = editPoint.cycleActions ?: emptyList()

                        if (!isDefaultEditing) {
                            cycleStages.forEachIndexed { index, stage ->
                                val stageLabel = actionLabel(stage.action)
                                val stageActionColor = stage.action.actionColor(extraColors)

                                Column(
                                    modifier = Modifier.dragonSettingGroup(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text =
                                                stringResource(
                                                    R.string.cycle_actions_stage,
                                                    index + 1
                                                ),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        DragonIconButton(
                                            icon = R.drawable.close,
                                            contentDescription = R.string.disable,
                                            isCancel = true
                                        ) {
                                            val updated =
                                                cycleStages
                                                    .toMutableList()
                                                    .also { it.removeAt(index) }
                                            editPoint =
                                                if (updated.isEmpty()) {
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
                                        this.Spacer()
                                        Icon(
                                            painter = painterResource(R.drawable.edit_rounded),
                                            contentDescription = stringResource(R.string.edit_action),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    this@DragonSettingsGroup.SliderWithLabel(
                                        label = stringResource(R.string.cycle_actions_delay),
                                        value = stage.triggerTimeMs,
                                        valueRange = 100..5000,
                                        resetEnabled =
                                            stage.triggerTimeMs != (
                                                defaultPoint.cycleActionsLoopDelayMs
                                                    ?: Point.defaultCycleActionsLoopDelayMs
                                            ),
                                        onReset = {
                                            val updated =
                                                cycleStages.toMutableList().also {
                                                    it[index] =
                                                        it[index].copy(
                                                            triggerTimeMs =
                                                                defaultPoint.cycleActionsLoopDelayMs
                                                                    ?: Point.defaultCycleActionsLoopDelayMs
                                                        )
                                                }
                                            editPoint = editPoint.copy(cycleActions = updated)
                                        }
                                    ) { newDelay ->
                                        val updated =
                                            cycleStages.toMutableList().also {
                                                it[index] = it[index].copy(triggerTimeMs = newDelay)
                                            }
                                        editPoint = editPoint.copy(cycleActions = updated)
                                    }

                                    this@DragonSettingsGroup.HapticFeedBackEditorButtonWithPlayTest(
                                        customHapticFeedback = stage.hapticFeedback ?: defaultHapticFeedback(),
                                        titleExt = " (Stage ${index + 1})",
                                        onClick = { editingCycleStageHapticIndex = index }
                                    )
                                }
                            }

                            DragonButton(
                                onClick = {
                                    val newStage =
                                        CycleActionStage(
                                            triggerTimeMs = 500,
                                            action = editPoint.action
                                        )
                                    editPoint =
                                        editPoint.copy(
                                            cycleActions = cycleStages + newStage
                                        )
                                }
                            ) {
                                Text(stringResource(R.string.cycle_actions_add_stage))
                            }
                        }

                        this.AnimatedVisibility(cycleStages.isNotEmpty() || isDefaultEditing) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val cycleActionLoop = editPoint.getCycleActionsStageLoop(defaultPoint, isDefaultEditing)
                                this@DragonSettingsGroup.SwitchRow(
                                    state = cycleActionLoop,
                                    title = R.string.cycle_actions_loop,
                                    description = R.string.cycle_actions_loop_desc
                                ) { on ->
                                    editPoint =
                                        editPoint.copy(
                                            cycleActionsLoop =
                                                on.takeIf {
                                                    it !=
                                                        emptyPoint.getCycleActionsStageLoop(
                                                            defaultPoint,
                                                            isDefaultEditing
                                                        )
                                                }
                                        )
                                }

                                this.AnimatedVisibility(cycleActionLoop) {
                                    this@DragonSettingsGroup.SliderWithLabel(
                                        label = stringResource(R.string.cycle_actions_loop_delay),
                                        value = editPoint.getCycleActionsStageLoopDelayMs(defaultPoint, isDefaultEditing),
                                        valueRange = 50..5000,
                                        resetEnabled = true,
                                        onReset = {
                                            editPoint = editPoint.copy(cycleActionsLoopDelayMs = null)
                                        }
                                    ) { ms ->
                                        editPoint =
                                            editPoint.copy(
                                                cycleActionsLoopDelayMs =
                                                    ms.takeIf {
                                                        it !=
                                                            emptyPoint.getCycleActionsStageLoopDelayMs(
                                                                defaultPoint,
                                                                isDefaultEditing
                                                            )
                                                    }
                                            )
                                    }
                                }
                            }
                        }
                    }

                    PointFeaturePanel.HoldAndRun -> {
                        if (isDefaultEditing) {
                            this.SliderWithLabel(
                                label = stringResource(R.string.default_hold_and_run_delay),
                                description = stringResource(R.string.default_hold_and_run_delay_summary),
                                value = editPoint.getHoldAndRunDelayMs(defaultPoint, true),
                                valueRange = 100..5000,
                                resetEnabled = editPoint.holdAndRunDelayMs != null,
                                onReset = {
                                    editPoint = editPoint.copy(holdAndRunDelayMs = null)
                                }
                            ) {
                                editPoint =
                                    editPoint.copy(
                                        holdAndRunDelayMs =
                                            it.takeIf {
                                                it !=
                                                    emptyPoint.getHoldAndRunDelayMs(
                                                        defaultPoint,
                                                        true
                                                    )
                                            }
                                    )
                            }
                        } else {
                            val harEnabled = editPoint.holdAndRunDelayMs != null

                            if (!harEnabled) {
                                DragonButton(
                                    onClick = {
                                        editPoint =
                                            editPoint.copy(
                                                holdAndRunDelayMs = defaultPoint.holdAndRunDelayMs ?: 1000
                                            )
                                    },
                                    modifier =
                                        Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.hold_and_run_enable))
                                }
                            } else {
                                this.SliderWithLabel(
                                    label = stringResource(R.string.hold_and_run_delay),
                                    value = editPoint.getHoldAndRunDelayMs(defaultPoint, false),
                                    valueRange = 50..5000,
                                    resetEnabled = editPoint.holdAndRunDelayMs != null,
                                    onReset = {
                                        editPoint = editPoint.copy(holdAndRunDelayMs = null)
                                    }
                                ) { newDelay ->
                                    editPoint =
                                        editPoint.copy(
                                            holdAndRunDelayMs =
                                                newDelay.takeIf {
                                                    it !=
                                                        emptyPoint.getHoldAndRunDelayMs(
                                                            defaultPoint,
                                                            false
                                                        )
                                                }
                                        )
                                }

                                this.SwitchRow(
                                    state = editPoint.holdAndRunAction != null,
                                    title = R.string.hold_and_run_custom_action,
                                    description = R.string.hold_and_run_custom_action_desc
                                ) { on ->
                                    editPoint =
                                        if (on) {
                                            showHoldAndRunActionDialog = true
                                            editPoint.copy(
                                                holdAndRunAction =
                                                    editPoint.holdAndRunAction
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
                                        modifier =
                                            Modifier
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
                                        editPoint =
                                            editPoint.copy(
                                                holdAndRunDelayMs = null,
                                                holdAndRunAction = null
                                            )
                                    },
                                    isCancel = true
                                ) {
                                    Text(
                                        text = stringResource(R.string.disable),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    null -> {
                        null
                    }
                }
            }

            if (!isDefaultEditing) {
                DragonSettingsGroup(R.string.name_and_action) {
                    DragonButton(onClick = { showEditActionDialog = true }) {
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

                    TextRow(
                        currentValue = editPoint.customName ?: "",
                        defaultValue = "",
                        label = stringResource(R.string.custom_name),
                        placeHolder = null,
                        singleChar = false
                    ) { editPoint = editPoint.copy(customName = it) }

                    // Only enable the color picker when the point has a customizable action
                    ColorPickerRow(
                        title = stringResource(R.string.custom_action_color),
                        description = null,
                        currentColor = editPoint.customActionColor,
                        defaultColor = null,
                        enabled = editPoint.action !is Action.LaunchApp && editPoint.action !is Action.LaunchShortcut
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
                    editPoint =
                        editPoint.copy(
                            innerPadding =
                                it.takeIf {
                                    it.value.round(2) !=
                                        emptyPoint
                                            .getInnerPadding(
                                                defaultPoint,
                                                isDefaultEditing
                                            ).value
                                            .round(2)
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
                    editPoint =
                        editPoint.copy(
                            size =
                                it.takeIf {
                                    it.value.round(2) !=
                                        emptyPoint
                                            .getSize(
                                                defaultPoint,
                                                isDefaultEditing
                                            ).value
                                            .round(2)
                                }
                        )
                }
            }

            DragonSettingsGroup(R.string.appearance) {
                if (!isDefaultEditing) {
                    DragonButton(onClick = { showEditIconDialog = true }) {
                        Text(stringResource(R.string.edit_icon))
                        Spacer(5.dp)
                        Icon(
                            painter = painterResource(R.drawable.edit_rounded),
                            contentDescription = stringResource(R.string.edit_icon)
                        )
                    }
                }
            }

            SingleSelectConnectedButtonRow(
                entries = SelectedUnselectedViewMode.entries,
                checked = { selectedView == it }
            ) { selectedView = it }

            AnimatedContent(targetState = selectedView) { view ->
                DragonSettingsGroup(R.string.fine_grained) {
                    val selected =
                        when (view) {
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
                            editPoint =
                                editPoint.copy(
                                    borderStrokeSelected =
                                        newValue.takeIf {
                                            it.value.round(2) !=
                                                emptyPoint
                                                    .getBorderStroke(
                                                        true,
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    ).value
                                                    .round(2)
                                        }
                                )
                        }

                        ColorPickerRow(
                            title = stringResource(R.string.border_color_selected),
                            description = null,
                            currentColor = editPoint.getBorderColor(true, defaultPoint, extraColors, isDefaultEditing),
                            defaultColor = null
                        ) { selectedColor ->
                            editPoint =
                                editPoint.copy(
                                    borderColorSelected =
                                        selectedColor.takeIf {
                                            it !=
                                                emptyPoint.getBorderColor(
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
                            currentColor = editPoint.getBackgroundColor(true, defaultPoint, isDefaultEditing),
                            defaultColor = null
                        ) { selectedColor ->
                            editPoint =
                                editPoint.copy(
                                    backgroundColorSelected =
                                        selectedColor.takeIf {
                                            it !=
                                                emptyPoint.getBackgroundColor(
                                                    true,
                                                    defaultPoint,
                                                    isDefaultEditing
                                                )
                                        }
                                )
                        }

                        SliderWithLabel(
                            label = stringResource(R.string.glow_radius),
                            description = stringResource(R.string.zero_means_no_glow),
                            value = editPoint.getGlow(true, defaultPoint, isDefaultEditing).radius!!,
                            valueRange = 0.dp..200.dp,
                            decimals = 1,
                            resetEnabled = editPoint.glowSelected?.radius != null,
                            onReset = {
                                editPoint = editPoint.copy(glowSelected = editPoint.glowSelected?.copy(radius = null).takeIf { it.isSpecified })
                            }
                        ) { newGlowRadius ->
                            editPoint =
                                editPoint.copy(
                                    glowSelected =
                                        (
                                            editPoint.glowSelected
                                                ?.copy(radius = newGlowRadius)
                                                ?: CustomGlow(radius = newGlowRadius)
                                        ).takeIf { it.isSpecified }
                                )
                        }

                        ColorPickerRow(
                            title = stringResource(R.string.glow_color),
                            description = null,
                            enabled = true,
                            currentColor = editPoint.getGlow(true, defaultPoint, isDefaultEditing).color,
                            defaultColor = null
                        ) { newColor ->
                            editPoint =
                                editPoint.copy(
                                    glowSelected =
                                        (
                                            editPoint.glowSelected
                                                ?.copy(color = newColor)
                                                ?: CustomGlow(color = newColor)
                                        ).takeIf { it.isSpecified }
                                )
                        }

                        ShapeRow(
                            selected = editPoint.getBorderShape(true, defaultPoint, isDefaultEditing),
                            title = stringResource(R.string.edit_border_shape),
                            resetEnabled = editPoint.borderShapeSelected != null,
                            onReset = {
                                editPoint = editPoint.copy(borderShapeSelected = null)
                            }
                        ) { showShapeSelectedPickerDialog = true }
                    } else {
                        SliderWithLabel(
                            label = stringResource(R.string.border_stroke),
                            value = editPoint.getBorderStroke(false, defaultPoint, isDefaultEditing),
                            valueRange = 0.dp..50.dp,
                            resetEnabled = editPoint.borderStroke != null,
                            onReset = { editPoint = editPoint.copy(borderStroke = null) }
                        ) { newValue ->
                            editPoint =
                                editPoint.copy(
                                    borderStroke =
                                        newValue.takeIf {
                                            it.value.round(2) !=
                                                emptyPoint
                                                    .getBorderStroke(
                                                        false,
                                                        defaultPoint,
                                                        isDefaultEditing
                                                    ).value
                                                    .round(2)
                                        }
                                )
                        }

                        ColorPickerRow(
                            title = stringResource(R.string.border_color),
                            description = null,
                            currentColor = editPoint.getBorderColor(false, defaultPoint, extraColors, isDefaultEditing),
                            defaultColor = null
                        ) { selectedColor ->
                            editPoint =
                                editPoint.copy(
                                    borderColor =
                                        selectedColor.takeIf {
                                            it !=
                                                emptyPoint.getBorderColor(
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
                            currentColor = editPoint.getBackgroundColor(false, defaultPoint, isDefaultEditing),
                            defaultColor = null
                        ) { selectedColor ->
                            editPoint =
                                editPoint.copy(
                                    backgroundColor =
                                        selectedColor.takeIf {
                                            it !=
                                                emptyPoint.getBackgroundColor(
                                                    false,
                                                    defaultPoint,
                                                    isDefaultEditing
                                                )
                                        }
                                )
                        }

                        SliderWithLabel(
                            label = stringResource(R.string.glow_radius),
                            description = stringResource(R.string.zero_means_no_glow),
                            value = editPoint.getGlow(false, defaultPoint, isDefaultEditing).radius!!,
                            valueRange = 0.dp..200.dp,
                            decimals = 1,
                            resetEnabled = editPoint.glow?.radius != null,
                            onReset = {
                                editPoint = editPoint.copy(glow = editPoint.glow?.copy(radius = null).takeIf { it.isSpecified })
                            }
                        ) { newGlowRadius ->
                            editPoint =
                                editPoint.copy(
                                    glow =
                                        (
                                            editPoint.glow
                                                ?.copy(radius = newGlowRadius)
                                                ?: CustomGlow(radius = newGlowRadius)
                                        ).takeIf { it.isSpecified }
                                )
                        }

                        ColorPickerRow(
                            title = stringResource(R.string.glow_color),
                            description = null,
                            enabled = true,
                            currentColor = editPoint.getGlow(false, defaultPoint, isDefaultEditing).color,
                            defaultColor = null
                        ) { newColor ->
                            editPoint =
                                editPoint.copy(
                                    glow =
                                        (
                                            editPoint.glow
                                                ?.copy(color = newColor)
                                                ?: CustomGlow(color = newColor)
                                        ).takeIf { it.isSpecified }
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

            if (!isDefaultEditing) {
                DragonSettingsGroup(R.string.haptic_feedback) {
                    HapticFeedBackEditorButtonWithPlayTest(
                        customHapticFeedback = editPoint.haptic ?: defaultHapticFeedback(),
                        onClick = { showHapticFeedbackEditor = true }
                    )
                }
            }
        }
    }

    if (showEditIconDialog) {
        PointIconEditor(editPoint) { newIcon, newProperties ->
            showEditIconDialog = false
            editPoint =
                editPoint.copy(
                    customIcon = newIcon,
                    iconProperties = newProperties?.takeIf { it.isNotEmpty }
                )
        }
    }

    if (showEditActionDialog) {
        ActionPickerDialog(
            onDismiss = { showEditActionDialog = false },
            onActionSelected = { selectedAction ->
                editPoint = editPoint.copy(action = selectedAction)
                showEditActionDialog = false
            }
        )
    }

    if (showHoldAndRunActionDialog) {
        ActionPickerDialog(
            onDismiss = { showHoldAndRunActionDialog = false },
            onActionSelected = { selectedAction ->
                editPoint = editPoint.copy(holdAndRunAction = selectedAction)
                showHoldAndRunActionDialog = false
            }
        )
    }

    if (showShapePickerDialog) {
        ShapePickerDialog(
            selected = editPoint.getBorderShape(false, defaultPoint, isDefaultEditing),
            onDismiss = { showShapePickerDialog = false }
        ) { newShape ->
            editPoint =
                editPoint.copy(
                    borderShape =
                        newShape.takeIf {
                            it !=
                                emptyPoint.getBorderShape(
                                    false,
                                    defaultPoint,
                                    isDefaultEditing
                                )
                        }
                )
        }
    }

    if (showShapeSelectedPickerDialog) {
        ShapePickerDialog(
            selected = editPoint.getBorderShape(true, defaultPoint, isDefaultEditing),
            onDismiss = { showShapeSelectedPickerDialog = false }
        ) { newShape ->
            editPoint =
                editPoint.copy(
                    borderShapeSelected =
                        newShape.takeIf {
                            it !=
                                emptyPoint.getBorderShape(
                                    true,
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
            editPoint =
                editPoint.copy(
                    borderShapeSelected =
                        newShape.takeIf {
                            it !=
                                emptyPoint.getBorderShape(
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
        ActionPickerDialog(
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
                val updated =
                    currentStages
                        .toMutableList()
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
                editPoint =
                    editPoint.copy(
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
