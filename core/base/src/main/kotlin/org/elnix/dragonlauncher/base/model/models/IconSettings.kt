package org.elnix.dragonlauncher.base.model.models
public data class IconSettings(
    val iconPack: String? = null,
    val iconsTint: Int? = null,
    val themedIcons: Boolean = false,
    val forceThemed: Boolean = false,
    val adaptify: Boolean = false,
    val onlyTintIconPacks: Boolean = true,
    val renderForeground: Boolean = true,
    val renderBackground: Boolean = true
)