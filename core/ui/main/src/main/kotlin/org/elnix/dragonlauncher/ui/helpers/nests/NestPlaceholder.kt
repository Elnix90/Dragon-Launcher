package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

@Suppress("FunctionName")
fun DrawScope.NestPlaceholder(
    center: Offset,
    drawParams: DrawParams
) {
    drawCircle(
        color = drawParams.extraColors.circle,
        center = center,
        radius = 100f
    )
}

//@Suppress("FunctionName")
//fun DrawScope.NestPlaceholder2(
//    center: Offset,
//    drawParams: DrawParams
//) {
//
//    drawImage()
//}