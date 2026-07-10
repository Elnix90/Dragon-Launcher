package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

@Suppress("FunctionName")
public fun DrawScope.NestPlaceholder(
    center: Offset,
    drawParams: DrawParams
) {
    drawCircle(
        color = drawParams.extraColors.shapes,
        center = center,
        radius = 100f
    )
}
