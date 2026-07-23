package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.isNotDefault
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.components.IntersectionShapePreview
import org.elnix.dragonlauncher.ui.components.NestNameEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun NestEditor(
    currentNest: Nest,
    defaultNest: Nest,
    defaultShape: IntersectionShape,
    isDefaultEditing: Boolean,
    tempCancelZone: Int,
    onEdit: (Nest) -> Unit,
    onReset: () -> Unit,
    onUpdateShapes: (netOffsetChange: Offset, newShapes: Set<IntersectionShape>) -> Unit,
    onUpdateCancelZone: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var showNestShapesManagementDialog by remember { mutableStateOf(false) }

    val shapes = currentNest.getInterSectionShapes(defaultNest, isDefaultEditing)
    val nestDebugInfo by DebugSettingsStore.nestDebugInfo.asState()

    DragonModalBottomSheet(onDismiss) {
        Column(
            modifier = Modifier
                .heightIn(max = 700.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DragonSettingsGroup(
                title = if (!isDefaultEditing) R.string.edit_nest else R.string.edit_default_nest,
                trailingIcon = { ResetIcon(currentNest.isNotDefault, onReset) }
            ) {

                if (nestDebugInfo) {
                    Text(
                        text = currentNest.toString(),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(5.dp)
                    )
                }
                DragonButton(
                    onClick = { showNestShapesManagementDialog = true },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.edit_shapes),
                        style = MaterialTheme.typography.labelMediumEmphasized
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End)
                    ) {
                        shapes.sortedBy { it.id }.forEach {
                            IntersectionShapePreview(it, defaultShape, size = 20.dp)
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(10.dp))

                if (!isDefaultEditing) {
                    NestNameEditor(
                        currentNest, Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        onEdit(currentNest.copy(name = it))
                    }
                }
                val showAllPointsInCurrentShape by UiSettingsStore.showAllPointsInCurrentShape.asState()

                SliderWithLabel(
                    label = stringResource(R.string.cancel_zone),
                    description = stringResource(R.string.cancel_zone_desc),
                    value = if (isDefaultEditing) currentNest.getCancelZone(defaultNest, true) else tempCancelZone,
                    valueRange = 0..300,
                    resetEnabled = currentNest.cancelZone != null,
                    onReset = {
                        onUpdateCancelZone(null)
                        onEdit(currentNest.copy(cancelZone = null))
                    },
                    onDragStateChange = { isDragging ->
                        if (!isDragging) {
                            onEdit(currentNest.copy(cancelZone = tempCancelZone))
                        }
                    },
                    onChange = onUpdateCancelZone
                )

                SwitchRow(
                    state = currentNest.getShowAllPointsInCurrentShape(defaultNest, showAllPointsInCurrentShape, isDefaultEditing),
                    title = stringResource(R.string.show_all_actions_on_current_shape),
                    description = stringResource(R.string.show_all_actions_on_current_shape_desc),
                    resetEnabled = currentNest.showAllPointsInCurrentShape != null,
                    onReset = { onEdit(currentNest.copy(showAllPointsInCurrentShape = null)) }
                ) { value -> onEdit(currentNest.copy(showAllPointsInCurrentShape = value)) }

                val showAllPointsInCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asState()
                SwitchRow(
                    state = currentNest.getShowAllPointsInCurrentNest(defaultNest, showAllPointsInCurrentNest, isDefaultEditing),
                    title = stringResource(R.string.show_all_actions_in_current_nest),
                    description = stringResource(R.string.show_all_actions_in_current_nest_desc),
                    resetEnabled = currentNest.showAllPointsInCurrentNest != null,
                    onReset = { onEdit(currentNest.copy(showAllPointsInCurrentNest = null)) }
                ) { value -> onEdit(currentNest.copy(showAllPointsInCurrentNest = value)) }

                val showCurrentShape by UiSettingsStore.showCurrentShape.asState()
                SwitchRow(
                    state = currentNest.getShowCurrentShape(defaultNest, showCurrentShape, isDefaultEditing),
                    title = stringResource(R.string.show_shape),
                    description = stringResource(R.string.show_shape_desc),
                    resetEnabled = currentNest.showCurrentShape != null,
                    onReset = { onEdit(currentNest.copy(showCurrentShape = null)) }
                ) { value -> onEdit(currentNest.copy(showCurrentShape = value)) }

                val showAllShapesInNest by UiSettingsStore.showAllShapesInNest.asState()
                SwitchRow(
                    state = currentNest.getShowAllShapes(defaultNest, showAllShapesInNest, isDefaultEditing),
                    title = stringResource(R.string.show_all_shapes),
                    description = stringResource(R.string.show_all_shapes_desc),
                    resetEnabled = currentNest.showAllShapes != null,
                    onReset = {
                        onEdit(currentNest.copy(showAllShapes = null))
                    }
                ) { value ->
                    onEdit(currentNest.copy(showAllShapes = value))
                }
            }
        }
    }
    val defaultShapes = defaultNest.getInterSectionShapes(defaultNest, isDefaultEditing)

    if (showNestShapesManagementDialog) {
        NestShapesManagementEditor(
            shapes = shapes,
            isDefaultEditing = isDefaultEditing,
            defaultShape = defaultShape,
            defaultShapes = defaultShapes,
            onReset = onReset,
            onUpdateShapes = onUpdateShapes,
        ) { newShapes ->
            onEdit(currentNest.copy(intersectionShapes = newShapes.takeIf { it != defaultShapes }))

            showNestShapesManagementDialog = false
        }
    }
}