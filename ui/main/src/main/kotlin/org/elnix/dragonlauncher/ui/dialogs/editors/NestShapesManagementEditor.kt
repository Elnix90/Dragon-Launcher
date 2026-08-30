package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.cleanString
import org.elnix.dragonlauncher.ktx.getNextId
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.selfAlignHorizontally
import org.elnix.dragonlauncher.ui.components.IntersectionShapePreview
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.components.CopyIcon
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

/**
 * Nest shapes management editor.
 * Only used by the [NestEditor], serves as a proxy to avoid writing a 500 lines file.
 *
 * @param shapesInternal list if shapes, passed from the [NestEditor] directly
 * @param triggerUpdate called when the ui needs a reactive update as comose isn't listening to every change in the [shapesInternal] list
 *
 * @see NestEditor for the other args
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestShapesManagementEditor(
    shapesInternal: SnapshotStateMap<Int, IntersectionShape>,
    triggerUpdate: () -> Unit,
    defaultShapes: Set<IntersectionShape>,
    isDefaultEditing: Boolean,
    defaultShape: IntersectionShape,
    modifier: Modifier = Modifier,
    onDismiss: (newShapes: Set<IntersectionShape>) -> Unit
) {
    fun updateShape(id: Int, newShape: (IntersectionShape) -> IntersectionShape) {
        val oldShape = shapesInternal[id] ?: return
        shapesInternal[id] = newShape(oldShape)
        triggerUpdate()
    }

    var showDetails by remember { mutableStateOf<Int?>(null) }

    DragonModalBottomSheet(
        onDismissRequest = {
            onDismiss(shapesInternal.values.toSet())
        }
    ) {
        DialogTitle(
            text = stringResource(R.string.shapes_management),
            resetEnabled = shapesInternal.values.toSet() != defaultShapes,
            onReset = {
                shapesInternal.clear()
                defaultShapes.forEach {
                    shapesInternal[it.id] = it
                }
                triggerUpdate()
            }
        )

        Spacer(5.dp)

        DragonButton(
            onClick = {
                val newId = shapesInternal.keys.getNextId()
                shapesInternal[newId] = IntersectionShape(newId)
                triggerUpdate()
            },
            modifier = modifier.selfAlignHorizontally()
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = null
            )
            Spacer(5.dp)
            Text(stringResource(R.string.add_shape))
        }

        Spacer(10.dp)

        DragonSettingsGroup {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                items(shapesInternal.values.toList()) { shape ->
                    ShapeItem(
                        shape = shape,
                        isDefaultEditing = isDefaultEditing,
                        defaultShape = defaultShape,
                        onChangeShape = { newShape ->
                            updateShape(shape.id) { old ->
                                old.copy(shape = newShape)
                            }
                        },
                        onClone = {
                            val id = shapesInternal.keys.getNextId()
                            shapesInternal[id] = shape.copy(id = id)
                            triggerUpdate()
                        },
                        onDelete = {
                            shapesInternal.remove(shape.id)
                            triggerUpdate()
                        },
                        oClick = { showDetails = shape.id }
                    )
                }
            }
        }
    }

    if (showDetails != null) {
        val shapeId = showDetails!!
        val shape = shapesInternal[shapeId] ?: IntersectionShape(shapeId)

        IntersectionShapeEditor(
            shape = shape,
            isDefaultEditing = isDefaultEditing,
            defaultShape = defaultShape,
            onChangeShape = { new ->
                updateShape(shapeId) { _ -> new }
            },
            onReset = {
                updateShape(shapeId) {
                    IntersectionShape(shapeId)
                }
            }
        ) { showDetails = null }
    }
}

@Composable
private fun DragonGroupScope.ShapeItem(
    shape: IntersectionShape,
    defaultShape: IntersectionShape,
    isDefaultEditing: Boolean,
    onChangeShape: (newShape: IconShape) -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    oClick: () -> Unit
) {
    var showShapeDialog by remember { mutableStateOf(false) }

    DragonRow(
        onClick = oClick,
        modifier = Modifier.dragonSettingGroup()
    ) {
        IntersectionShapePreview(
            shape = shape,
            defaultShape = defaultShape,
            size = 50.dp,
            isDefaultEditing = isDefaultEditing
        ) { showShapeDialog = true }

        Spacer(12.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "${stringResource(R.string.shape_offset)}: ${shape.getOffset(defaultShape, isDefaultEditing).cleanString()}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = stringResource(R.string.shape_scale, shape.getScale(defaultShape, isDefaultEditing)),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = stringResource(R.string.rotation_arg, shape.getRotation(defaultShape, isDefaultEditing)),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer()

        CopyIcon(onCopy = onClone)

        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = R.string.remove,
            isCancel = true,
            onClick = onDelete
        )
    }

    if (showShapeDialog) {
        ShapePickerDialog(
            selected = shape.getShape(defaultShape, isDefaultEditing),
            allowedShapes = IconShape.allowedNestShapes,
            onDismiss = { showShapeDialog = false }
        ) {
            onChangeShape(it)
            showShapeDialog = false
        }
    }
}
