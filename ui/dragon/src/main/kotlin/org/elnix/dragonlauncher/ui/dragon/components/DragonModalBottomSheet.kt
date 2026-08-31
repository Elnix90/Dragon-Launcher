package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.elnix.dragonlauncher.ui.base.compositionlocals.LocalFullscreen

@Composable
@ExperimentalMaterial3Api
fun DragonModalBottomSheet(
    onDismissRequest: () -> Unit,
    skipPartiallyExpanded: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    DragonModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberBottomSheetState(skipPartiallyExpanded),
        content = content
    )
}

@Composable
@ExperimentalMaterial3Api
fun DragonModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentWindowInsets = {
            WindowInsets.safeDrawing.add(
                WindowInsets(
                    left = 15.dp,
                    right = 15.dp,
                    top = 0.dp,
                    bottom = 15.dp
                )
            )
        }
    ) {
        // AHAHAHAHAHAHAHAHA FUCK ITTT
        // FUUUUUUUUUUUCK ANDROID AND YOUR WINDOWS VIEWS!!!!
        // I FINALLY MANAGED TO MAKE IT!!!!!!!!!!
        // THE SHEET IS NOT FULLSCREEN WHEN IT APPEARS NOWWWWWW

        val view = LocalView.current
        val fullscreen = LocalFullscreen.current
        val dialogWindow = (view.parent as? DialogWindowProvider)?.window

        if (fullscreen) {
            SideEffect {
                dialogWindow?.let { window ->
                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }

        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberBottomSheetState(skipPartiallyExpanded: Boolean = false): SheetState =
    rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues =
            buildSet {
                add(SheetValue.Hidden)
                if (!skipPartiallyExpanded) add(SheetValue.PartiallyExpanded)
                add(SheetValue.Expanded)
            }
    )
