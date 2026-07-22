package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.cleanString
import org.elnix.dragonlauncher.ktx.getNextId
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.selfAlignHorizontally
import org.elnix.dragonlauncher.ui.components.IntersectionShapePreview
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@Composable
public fun NestShapesManagementEditor(
    shapes: Set<IntersectionShape>,
    isDefaultEditing: Boolean,
    defaultNest: Nest,
    defaultShape: IntersectionShape,
    modifier: Modifier = Modifier,
    onSave: (newShapes: Set<IntersectionShape>) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {

    val shapesInternal: SnapshotStateMap<Int, IntersectionShape> = remember(shapes) { mutableStateMapOf() }
    LaunchedEffect(defaultShape, shapes) {
        if (isDefaultEditing) {
            (defaultNest.intersectionShapes ?: Nest.defaultIntersectionShapes).forEach {
                shapesInternal[it.id] = it
            }
        } else {
            shapes.forEach {
                shapesInternal[it.id] = it
            }
        }
    }

    fun updateShape(id: Int, newShape: (IntersectionShape) -> IntersectionShape) {
        val oldShape = shapesInternal[id] ?: return
        shapesInternal[id] = newShape(oldShape)
    }

    var showDetails by remember { mutableStateOf<Int?>(null) }


    CustomAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .padding(30.dp)
            .heightIn(max = 600.dp),
        imePadding = false,
        scroll = false,
        alignment = Alignment.Center,
        confirmButton = {
            ValidateCancelButtons(stringResource(R.string.ok)) {
                onSave(shapesInternal.values.toSet())
                onDismiss()
            }
        },
        dismissButton = null,
        icon = null,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DialogTitle(stringResource(R.string.shapes_management))
                ResetIcon(onReset = onReset)
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DragonButton(
                onClick = {
                    val newId = shapesInternal.keys.getNextId()

                    shapesInternal[newId] = IntersectionShape(
                        id = newId,
                        shape = IconShape.Circle,
                        scale = 1.5f,
                        offset = Offset.Zero
                    )
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



            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp)
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
                        },
                        onDelete = {
                            shapesInternal.remove(shape.id)
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
private fun ShapeItem(
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
        modifier = Modifier.fillMaxWidth()
    ) {
        IntersectionShapePreview(
            shape = shape,
            defaultShape = defaultShape,
            size = 50.dp
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

        DragonIconButton(
            icon = R.drawable.copy,
            contentDescription = R.string.copy,
            onClick = onClone
        )

        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = R.string.remove,
            colors = AppObjectsColors.cancelIconButtonColors(),
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
