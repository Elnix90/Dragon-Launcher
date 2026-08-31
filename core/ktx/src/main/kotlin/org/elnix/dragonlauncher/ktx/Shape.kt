package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

public fun Shape.toPath(
    size: Size,
    density: Density
): Path {
    val outline =
        createOutline(
            size = size,
            layoutDirection = LayoutDirection.Ltr,
            density = density
        )

    return when (outline) {
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Generic -> outline.path
    }
}

public fun DrawScope.toPath(
    shape: Shape,
    size: Size
): Path = shape.toPath(size, this)
