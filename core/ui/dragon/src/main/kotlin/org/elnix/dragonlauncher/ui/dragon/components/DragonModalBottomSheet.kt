package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Used in modal sheets to give padding to the content to avoid it being directly on the edges
 */
val modalWindowInsets: @Composable (() -> WindowInsets)
    get() = {
        WindowInsets.safeDrawing.add(
            WindowInsets(
                left = 15.dp,
                right = 15.dp,
                top = 0.dp,
                bottom = 15.dp
            )
        )
    }



@Composable
@ExperimentalMaterial3Api
fun DragonModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor = BottomSheetDefaults.ContainerColor
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = BottomSheetDefaults.SheetMaxWidth,
        sheetGesturesEnabled = true,
        shape = BottomSheetDefaults.ExpandedShape,
        containerColor = containerColor,
        contentColor = contentColorFor(containerColor),
        tonalElevation = 0.dp,
        scrimColor = BottomSheetDefaults.ScrimColor,
        dragHandle = ::DragHandle,
        contentWindowInsets = modalWindowInsets,
        properties = properties,
        content = content
    )
}

@Composable
private fun DragHandle() {
    Surface(
        modifier = Modifier.padding(vertical = 22.dp),
        color = MaterialTheme.colorScheme.outline,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Box(Modifier.size(width = 32.dp, height = 4.dp))
    }
}