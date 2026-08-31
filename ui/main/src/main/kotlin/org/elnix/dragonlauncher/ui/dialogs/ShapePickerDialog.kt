package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IconShape.Companion.allShapes
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.helpers.ShapePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapePickerDialog(
    selected: IconShape,
    allowedShapes: Set<IconShape>? = null,
    onDismiss: () -> Unit,
    onPicked: (IconShape) -> Unit
) {
    val filteredShapes =
        remember(allowedShapes) {
            if (allowedShapes != null) {
                allShapes.filter { it in allowedShapes }
            } else {
                allShapes.toList()
            }
        }

    DragonModalBottomSheet(onDismissRequest = onDismiss) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(top = 10.dp, bottom = 50.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredShapes) { shape ->
                ShapePreview(
                    iconShape = shape,
                    size = 70.dp,
                    selected = shape == selected
                ) {
                    onPicked(shape)
                    onDismiss()
                }
            }
        }
    }
}
