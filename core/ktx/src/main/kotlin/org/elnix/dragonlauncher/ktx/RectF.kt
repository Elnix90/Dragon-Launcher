package org.elnix.dragonlauncher.ktx

import android.graphics.RectF

public fun RectF.scale(factor: Float) {
    val newWidth = width() * factor
    val newHeight = height() * factor
    bottom += newHeight - height()
    right += newWidth - width()
}

public fun RectF.translate(x: Float, y: Float): RectF {
    top += y
    bottom += y
    left += x
    right += x
    return this
}

public infix fun RectF.copyTo(other: RectF) {
    other.top = top
    other.left = left
    other.right = right
    other.bottom = bottom
}
