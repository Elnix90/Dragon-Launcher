package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.allShapes
import org.elnix.dragonlauncher.ui.base.components.sheets.DismissableBottomSheet
import org.elnix.dragonlauncher.ui.helpers.ShapePreview

@Composable
fun ShapePickerDialog(
    selected: IconShape,
    expanded: () -> Boolean,
    onDismiss: () -> Unit,
    onPicked: (IconShape) -> Unit
) {

    DismissableBottomSheet(
        onDismissRequest = onDismiss,
        expanded = expanded(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 50.dp)
        ) {

            items(allShapes) { shape ->
                ShapePreview(
                    iconShape = shape,
                    modifier = Modifier.height(70.dp),
                    selected = shape == selected
                ) {
                    onPicked(shape)
                    onDismiss()
                }
            }
        }
    }
}
