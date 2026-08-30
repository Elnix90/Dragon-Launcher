package org.elnix.dragonlauncher.ktx

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable

public fun LayerDrawable.getDrawableOrNull(index: Int): Drawable? {
    return try {
        this.getDrawable(index)
    } catch (e: IndexOutOfBoundsException) {
        return null
    }
}
