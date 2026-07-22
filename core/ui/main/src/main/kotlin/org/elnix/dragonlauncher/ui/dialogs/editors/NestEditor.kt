package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.takeIfNot
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.components.IntersectionShapePreview
import org.elnix.dragonlauncher.ui.components.NestNameEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow


@Composable
public fun NestEditor(
    currentNest: Nest,
    defaultNest: Nest,
    defaultShape: IntersectionShape,
    isDefaultEditing: Boolean,
    tempCancelZone: Int,
    onEdit: (Nest) -> Unit,
    onReset: () -> Unit,
    onUpdateCancelZone: (Int) -> Unit
) {
    var showNestShapesManagementDialog by remember { mutableStateOf(false) }

    /**
     * If the current nest is not a default nest with all null values
     */
    val canReset = currentNest != Nest(currentNest.id)


    DragonSettingsGroup(
        title = if (!isDefaultEditing) R.string.edit_nest else R.string.edit_default_nest,
        trailingIcon =  { ResetIcon(canReset, onReset) }
    ) {
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
                currentNest.getInterSectionShapes(defaultNest, isDefaultEditing).sortedBy { it.id }.forEach {
                    IntersectionShapePreview(it, defaultShape, size = 20.dp)
                }
            }
        }

        HorizontalDivider(Modifier.padding(10.dp))

        if (!isDefaultEditing) {
            NestNameEditor(currentNest, Modifier.padding(10.dp).fillMaxWidth()) {
                onEdit(currentNest.copy(name = it))
            }
        }
        val showAllPointsInCurrentShape by UiSettingsStore.showAllPointsInCurrentShape.asState()

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

        SliderWithLabel(
            label = stringResource(R.string.cancel_zone),
            description = stringResource(R.string.cancel_zone_desc),
            value = tempCancelZone,
            valueRange = 0..300,
            resetEnabled = tempCancelZone != currentNest.getCancelZone(defaultNest, isDefaultEditing),
            onReset = { onUpdateCancelZone(defaultNest.cancelZone.takeIfNot(isDefaultEditing) ?: Nest.defaultCancelZone) },
            onDragStateChange = { isDragging ->
                if (!isDragging) {
                    onEdit(currentNest.copy(cancelZone = tempCancelZone))
                }
            },
            onChange = onUpdateCancelZone
        )
    }

    if (showNestShapesManagementDialog) {
        NestShapesManagementEditor(
            shapes = currentNest.getInterSectionShapes(defaultNest, isDefaultEditing),
            isDefaultEditing = isDefaultEditing,
            defaultNest = defaultNest,
            defaultShape = defaultShape,
            onSave = {
                onEdit(currentNest.copy(intersectionShapes = it))
            },
            onReset = onReset
        ) { showNestShapesManagementDialog = false }
    }
}