package org.elnix.dragonlauncher.base.model.models

import androidx.compose.ui.graphics.Color

public data class IconSettings(
	val iconPack: String? = null,
	val iconsTint: Color? = null,
	val themedIcons: Boolean = false,
	val forceThemed: Boolean = false,
	val adaptify: Boolean = false,
	val onlyTintIconPacks: Boolean = true,
	val renderForeground: Boolean = true,
	val renderBackground: Boolean = true
)
