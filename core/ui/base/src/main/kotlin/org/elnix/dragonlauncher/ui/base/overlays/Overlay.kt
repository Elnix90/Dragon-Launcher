package org.elnix.dragonlauncher.ui.base.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

// Stolen from https://github.com/MM2-0/Kvaesitso/tree/d3c8eb7d699ed179fbf5799bcf02384be475dae2/app/ui/src/main/java/de/mm20/launcher2/ui/overlays

@Composable
fun Overlay(
    zIndex: Float = LocalZIndex.current + 1f,
    overlay: @Composable () -> Unit
) {
    val overlayManager = LocalOverlayManager.current
    DisposableEffect(Unit) {
        overlayManager.addOverlay(overlay, zIndex)
        onDispose {
            overlayManager.removeOverlay(overlay)
        }
    }
}