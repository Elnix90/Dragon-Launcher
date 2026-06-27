package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.ui.components.IntersectionShape
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog


// TODO
@Composable
fun IntersectionShapeEditor(
    shape: IntersectionShape,
    modifier: Modifier = Modifier,
    onDismiss: (IntersectionShape) -> Unit
) {

    var editShape by remember(shape) { mutableStateOf(shape) }

    fun updateShape(block: (IntersectionShape) -> IntersectionShape) {
        editShape = block(editShape)
    }

    var customOffset by remember { mutableStateOf(Offset.Zero) }
    val px: Dp = customOffset.x.toDp
    val py: Dp = customOffset.y.toDp

    CustomAlertDialog(
        onDismissRequest = { onDismiss(editShape) },
        alignment = Alignment.Center,
        imePadding = false,
        modifier = Modifier.padding(16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { _, dragAmount ->
                            customOffset += dragAmount
                        }
                    )
                }
        ) {
            IntersectionShape(
                editShape,
                modifier = Modifier.offset(px, py)
            )
        }
    }
}