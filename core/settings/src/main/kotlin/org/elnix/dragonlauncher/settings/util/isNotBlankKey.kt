package org.elnix.dragonlauncher.settings.util

val String.isNotBlankKey: String
    get() = this.ifEmpty { error("Key cannot be null or empty") }
