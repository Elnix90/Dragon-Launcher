package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionState

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
    sheetState: SheetState = rememberBottomSheetState(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberBottomSheetState(skipPartiallyExpanded: Boolean = false): SheetState =
    rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = buildSet {
            add(SheetValue.Hidden)
            if (!skipPartiallyExpanded) add(SheetValue.PartiallyExpanded)
            add(SheetValue.Expanded)
        }
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberBottomSheetState(state: ExpandableSectionState): SheetState =
    rememberBottomSheetState(state.mode is ExpandableSectionMode.ModalSheet && state.mode.skipPartiallyExpanded)
