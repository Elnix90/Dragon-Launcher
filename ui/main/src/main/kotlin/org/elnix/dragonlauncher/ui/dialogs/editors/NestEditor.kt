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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.emptyNest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.isNotDefault
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.components.IntersectionShapePreview
import org.elnix.dragonlauncher.ui.components.NestNameEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle


/**
 * Nest editor, provides [DragonModalBottomSheet] to edit the selected [currentNest].
 * It managed internally a variable state of the [currentNest], linked to any changes made to it that mutates internally.
 * When the user dismisses the Sheet, the internal copy is saved as the new nest via [onDismiss]
 *
 * @param currentNest the nest to edit
 * @param defaultNest the default nest, used to display defaults values when null is provided
 * @param defaultShape used by the shapes editor
 * @param isDefaultEditing whether the current nest is the default nest or not. default values aren't the same if it's the default nest
 * @param onUpdateShapes instant ui updates for displaying the new shapes to the user. should be kept in sync with the final saved result
 * @param tempCancelZone instant ui updates for the cancel zone
 * @param onUpdateCancelZone lambda that acts as a proxy between the slider onChange value and the [tempCancelZone] to display instant ui updates
 * @param onDismiss closes the Sheet and saves the values to [org.elnix.dragonlauncher.points.PointsService]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestEditor(
    currentNest: Nest,
    defaultNest: Nest,
    defaultShape: IntersectionShape,
    isDefaultEditing: Boolean,
    onUpdateShapes: (changedShapes: Map<IntersectionShape, Offset>) -> Unit,
    tempCancelZone: Dp,
    onUpdateCancelZone: (Dp?) -> Unit,
    onDismiss: (newNest: Nest, changedShapes: Map<IntersectionShape, Offset>) -> Unit
) {
    val nestDebugInfo by DebugSettingsStore.nestDebugInfo.asState()
    var showNestShapesManagementDialog by remember { mutableStateOf(false) }

    var editNest by remember(currentNest) { mutableStateOf(currentNest) }

    val shapes = editNest.getInterSectionShapes(defaultNest, isDefaultEditing)
    val defaultShapes = emptyNest.getInterSectionShapes(defaultNest, isDefaultEditing)

    val shapesInternal: SnapshotStateMap<Int, IntersectionShape> = remember {
        mutableStateMapOf<Int, IntersectionShape>().apply {
            shapes.forEach {
                this[it.id] = it
            }
        }
    }

    fun getChangedShapes(): Map<IntersectionShape, Offset> = shapesInternal.values.toSet().associateWith { shapeInternal ->
        val witnessShape = shapes.find { shapeInternal.id == it.id } ?: return@associateWith Offset.Zero
        shapeInternal.getOffset(defaultShape, isDefaultEditing) - witnessShape.getOffset(defaultShape, isDefaultEditing)
    }

    fun triggerUpdate() {
        onUpdateShapes(getChangedShapes())
    }

    DragonModalBottomSheet(
        onDismissRequest = {
            onDismiss(editNest, getChangedShapes())
        }
    ) {
        DialogTitle(
            text = stringResource(if (!isDefaultEditing) R.string.edit_nest else R.string.edit_default_nest),
            resetEnabled = editNest.isNotDefault
        ) { editNest = emptyNest }

        Column(
            modifier = Modifier
                .heightIn(max = 700.dp)
                .verticalScroll(rememberScrollState())
        ) {

            if (nestDebugInfo) {
                Text(
                    text = editNest.toString(),
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
                    editNest, Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    editNest = editNest.copy(name = it)
                }
            }
            val showAllPointsInCurrentShape by UiSettingsStore.showAllPointsInCurrentShape.asState()

            SliderWithLabel(
                label = stringResource(R.string.cancel_zone),
                description = stringResource(R.string.cancel_zone_desc),
                value = if (isDefaultEditing) editNest.getCancelZone(defaultNest, true) else tempCancelZone,
                valueRange = 0.dp..300.dp,
                resetEnabled = editNest.cancelZone != null,
                onReset = {
                    onUpdateCancelZone(null)
                    editNest = editNest.copy(cancelZone = null)
                },
                onDragStateChange = { isDragging ->
                    if (!isDragging) {
                        editNest = editNest.copy(cancelZone = tempCancelZone)
                    }
                },
                onChange = onUpdateCancelZone
            )

            SwitchRow(
                state = editNest.getShowAllPointsInCurrentShape(defaultNest, showAllPointsInCurrentShape, isDefaultEditing),
                title = stringResource(R.string.show_all_actions_on_current_shape),
                description = stringResource(R.string.show_all_actions_on_current_shape_desc),
                resetEnabled = editNest.showAllPointsInCurrentShape != null,
                onReset = { editNest = editNest.copy(showAllPointsInCurrentShape = null) }
            ) { value -> editNest = editNest.copy(showAllPointsInCurrentShape = value) }

            val showAllPointsInCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asState()
            SwitchRow(
                state = editNest.getShowAllPointsInCurrentNest(defaultNest, showAllPointsInCurrentNest, isDefaultEditing),
                title = stringResource(R.string.show_all_actions_in_current_nest),
                description = stringResource(R.string.show_all_actions_in_current_nest_desc),
                resetEnabled = editNest.showAllPointsInCurrentNest != null,
                onReset = { editNest = editNest.copy(showAllPointsInCurrentNest = null) }
            ) { value -> editNest = editNest.copy(showAllPointsInCurrentNest = value) }

            val showCurrentShape by UiSettingsStore.showCurrentShape.asState()
            SwitchRow(
                state = editNest.getShowCurrentShape(defaultNest, showCurrentShape, isDefaultEditing),
                title = stringResource(R.string.show_shape),
                description = stringResource(R.string.show_shape_desc),
                resetEnabled = editNest.showCurrentShape != null,
                onReset = { editNest = editNest.copy(showCurrentShape = null) }
            ) { value -> editNest = editNest.copy(showCurrentShape = value) }

            val showAllShapesInNest by UiSettingsStore.showAllShapesInNest.asState()
            SwitchRow(
                state = editNest.getShowAllShapes(defaultNest, showAllShapesInNest, isDefaultEditing),
                title = stringResource(R.string.show_all_shapes),
                description = stringResource(R.string.show_all_shapes_desc),
                resetEnabled = editNest.showAllShapes != null,
                onReset = {
                    editNest = editNest.copy(showAllShapes = null)
                }
            ) { value ->
                editNest = editNest.copy(showAllShapes = value)
            }

            var tempScaleFactor by remember(
                editNest.previewScaleFactor,
                defaultNest.previewScaleFactor
            ) { mutableFloatStateOf(editNest.getPreviewScaleFactor(defaultNest, isDefaultEditing)) }
            
            SliderWithLabel(
                label = stringResource(R.string.preview_scale_factor),
                description = stringResource(R.string.preview_scale_factor_desc),
                value = tempScaleFactor,
                valueRange = 0f..5f,
                resetEnabled = editNest.previewScaleFactor != null,
                onReset = { editNest = editNest.copy(previewScaleFactor = null) },
                onDragStateChange = { isDragging ->
                    if (!isDragging) {
                        editNest = editNest.copy(previewScaleFactor = tempScaleFactor)
                    }
                },
                onChange = { tempScaleFactor = it }
            )
        }
    }

    if (showNestShapesManagementDialog) {
        NestShapesManagementEditor(
            shapesInternal = shapesInternal,
            triggerUpdate = ::triggerUpdate,
            isDefaultEditing = isDefaultEditing,
            defaultShape = defaultShape,
            defaultShapes = defaultShapes,
            onUpdateShapes = onUpdateShapes,
        ) { newShapes ->
            editNest = editNest.copy(intersectionShapes = newShapes.takeIf { it != defaultShapes })
            showNestShapesManagementDialog = false
        }
    }
}