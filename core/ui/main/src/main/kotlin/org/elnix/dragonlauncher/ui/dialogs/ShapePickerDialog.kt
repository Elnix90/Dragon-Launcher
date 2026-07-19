package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
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
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip
import org.elnix.dragonlauncher.ui.helpers.ShapePreview
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ShapePickerDialog(
    selected: IconShape,
    allowedShapes: Set<KClass<out IconShape>>? = null,
    onDismiss: () -> Unit,
    onPicked: (IconShape) -> Unit
) {

    val filteredShapes = remember(allowedShapes) {
       if (allowedShapes != null) {
           allShapes.filter { it::class in allowedShapes }
       } else allShapes.toList()
    }

    DragonModalBottomSheet(onDismissRequest = onDismiss) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 50.dp)
        ) {

            items(filteredShapes) { shape ->
                DragonTooltip(
                    modifier = Modifier.height(70.dp),
                    description = shape.toString()
                ) {
                    ShapePreview(
                        iconShape = shape,
                        modifier = it,
                        selected = shape == selected
                    ) {
                        onPicked(shape)
                        onDismiss()
                    }
                }
            }
        }
    }
}
