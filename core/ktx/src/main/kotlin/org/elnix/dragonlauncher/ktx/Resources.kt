package org.elnix.dragonlauncher.ktx

import android.content.res.Resources
import android.content.res.TypedArray

fun Resources.obtainTypedArrayOrNull(id: Int): TypedArray? =
	try {
		obtainTypedArray(id)
	} catch (_: Resources.NotFoundException) {
		null
	}
