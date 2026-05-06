package org.elnix.dragonlauncher.ui.base.overlays

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

// Stolen from https://github.com/MM2-0/Kvaesitso/tree/d3c8eb7d699ed179fbf5799bcf02384be475dae2/app/ui/src/main/java/de/mm20/launcher2/ui/overlays


@Composable
fun OverlayHost(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val overlayManager = remember { OverlayManager() }
    CompositionLocalProvider(LocalOverlayManager provides overlayManager) {
        Box {
            Box(
                contentAlignment = contentAlignment,
                modifier = modifier.zIndex(0f),
            ) {
                content()
            }
            for (overlay in overlayManager.overlays) {
                Box(modifier = Modifier.zIndex(overlay.zIndex)) {
                    CompositionLocalProvider(
                        LocalZIndex provides overlay.zIndex
                    ) {
                        overlay()
                    }
                }
            }
        }
    }
}

val LocalOverlayManager = compositionLocalOf {
    OverlayManager()
}

val LocalZIndex = compositionLocalOf {
    0f
}