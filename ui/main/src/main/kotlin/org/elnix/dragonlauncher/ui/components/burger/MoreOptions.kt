package org.elnix.dragonlauncher.ui.components.burger

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class MoreOptions(
    val text: @Composable () -> String,
    val onClick: () -> Unit,
    val icon: Int,
    val tint: Color? = null,
    val enabled: Boolean = true,
    val disabledText: (@Composable () -> String)? = null,
)
