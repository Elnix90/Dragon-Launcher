package org.elnix.dragonlauncher.ktx

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.Drawable

public fun Resources.getIntArrayOrNull(id: Int): IntArray? =
    try {
        getIntArray(id)
    } catch (_: Resources.NotFoundException) {
        null
    }

@SuppressLint("UseCompatLoadingForDrawables")
public fun Resources.getDrawableOrNull(id: Int, theme: Resources.Theme? = null): Drawable? =
    try {
        getDrawable(id, theme)
    } catch (e: Resources.NotFoundException) {
        null
    }

public fun Resources.obtainTypedArrayOrNull(id: Int): TypedArray? =
    try {
        obtainTypedArray(id)
    } catch (_: Resources.NotFoundException) {
        null
    }
