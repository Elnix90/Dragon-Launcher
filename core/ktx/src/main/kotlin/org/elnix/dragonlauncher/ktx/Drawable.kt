package org.elnix.dragonlauncher.ktx

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap

fun Drawable.drawWithColorFilter(canvas: Canvas, colorFilter: ColorFilter?) {
	val cf = this.colorFilter
	this.colorFilter = colorFilter
	this.draw(canvas)
	this.colorFilter = cf
}
