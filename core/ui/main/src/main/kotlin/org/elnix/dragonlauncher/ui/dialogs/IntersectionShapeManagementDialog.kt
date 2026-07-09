package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getNextId
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.selfAlignHorizontally
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.ShapePreview

@Composable
public fun IntersectionShapeManagementDialog(
    shapes: Set<IntersectionShape>,
    modifier: Modifier = Modifier,
    onSelectShape: (id: Int) -> Unit,
    onSave: (newShapes: Set<IntersectionShape>) -> Unit,
    onDismiss: () -> Unit
) {

    val shapesInternal: SnapshotStateSet<IntersectionShape> = remember(shapes) {
        mutableStateSetOf<IntersectionShape>().apply {
            addAll(shapes)
        }
    }

    fun updateShape(id: Int, newShape: (IntersectionShape) -> IntersectionShape) {
        val oldShape = shapesInternal.find { it.id == id } ?: return
        shapesInternal -= oldShape
        shapesInternal += newShape(oldShape)
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.heightIn(max = 600.dp),
        imePadding = false,
        scroll = false,
        alignment = Alignment.Center,
        confirmButton = {
            ValidateCancelButtons(onCancel = onDismiss) {
                onSave(shapesInternal)
                onDismiss()
            }
        },
        dismissButton = null,
        icon = null,
        title = {
            DialogTitle(stringResource(R.string.shapes_management))
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(shapesInternal.toList()) { shape ->
                ShapeItem(
                    shape = shape,
                    onChangeShape = { newShape ->
                        updateShape(shape.id) { old ->
                            old.copy(shape = newShape)
                        }
                    },
                    onDelete = {
                        shapesInternal.removeIf { it.id == shape.id }
                    },
                    onSelect = { onSelectShape(shape.id) }
                )
            }

            item {
                DragonButton(
                    onClick = {
                        val newId = shapesInternal.mapTo(mutableSetOf()) { it.id }.getNextId()

                        shapesInternal.add(
                            IntersectionShape(
                                id = newId,
                                shape = IconShape.Circle,
                                scale = 1.5f,
                                offset = Offset.Zero
                            )
                        )
                    },
                    modifier = modifier.selfAlignHorizontally()
                ) {
                    Text(stringResource(R.string.add_shape))
                }
            }
        }
    }
}

@Composable
private fun ShapeItem(
    shape: IntersectionShape,
    onChangeShape: (newShape: IconShape) -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit
) {
    var showShapeDialog by remember { mutableStateOf(false) }

    DragonRow(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        ShapePreview(
            iconShape = shape.shape,
            modifier = Modifier.size(40.dp)
        ) { showShapeDialog = true }

        Text("ID: ${shape.id}")
        Spacer()


        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = shape.offset.toString(),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = stringResource(R.string.shape_scale, shape.scale),
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = stringResource(R.string.rotation_arg, shape.angle),
                style = MaterialTheme.typography.labelSmall
            )
        }


        DragonIconButton(
            icon = R.drawable.close,
            contentDescription = R.string.remove,
            onClick = onDelete
        )
    }

    if (showShapeDialog) {
        ShapePickerDialog(
            selected = shape.shape,
            onDismiss = { showShapeDialog = false }
        ) {
            onChangeShape(it)
            showShapeDialog = false
        }
    }
}