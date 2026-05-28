package org.elnix.dragonlauncher.ktx

inline fun <T> T?.or(block: () -> T?): T? = this ?: block()
