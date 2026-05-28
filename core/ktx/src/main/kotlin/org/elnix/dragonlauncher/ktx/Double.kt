package org.elnix.dragonlauncher.ktx

import kotlin.math.ceil

fun Double.ceilToInt(): Int {
    return ceil(this).toInt()
}
