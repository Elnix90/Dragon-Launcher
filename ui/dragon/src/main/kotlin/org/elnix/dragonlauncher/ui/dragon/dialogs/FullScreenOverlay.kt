package org.elnix.dragonlauncher.ui.dragon.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullScreenOverlay(
    onDismissRequest: () -> Unit,
    alignment: Alignment = Alignment.BottomCenter,
    imePadding: Boolean = true,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = null
                ) { onDismissRequest() },
            contentAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .then(if (imePadding) Modifier.imePadding() else Modifier)
                    // Consume clicks so they don't reach the scrim
                    .clickable(
                        indication = null,
                        interactionSource = null
                    ) { }
            ) {
                content()
            }
        }
    }
}
