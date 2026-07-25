package org.elnix.dragonlauncher.ui.base.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList


// Stolen from https://github.com/MM2-0/Kvaesitso/tree/d3c8eb7d699ed179fbf5799bcf02384be475dae2/app/ui/src/main/java/de/mm20/launcher2/ui/overlays

class OverlayManager {
    val overlays: SnapshotStateList<Overlay> = mutableStateListOf()

    fun addOverlay(overlay: @Composable () -> Unit, zIndex: Float = 0f) {
        overlays.add(Overlay(overlay, zIndex))
    }

    fun removeOverlay(overlay: @Composable () -> Unit) {
        overlays.removeAll { overlay == it.overlay }
    }
}

data class Overlay(
    val overlay: @Composable () -> Unit,
    val zIndex: Float = 0f,
) {
    @Composable
    operator fun invoke() {
        overlay()
    }
}