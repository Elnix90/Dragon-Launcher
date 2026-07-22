package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.runtime.Composable
import org.elnix.dragonlauncher.i18n.R

@Composable
public fun ResetIcon(enabled: Boolean = true, onReset: () -> Unit) {
    DragonIconButton(
        icon = R.drawable.reset,
        contentDescription = R.string.reset,
        enabled = enabled,
        onClick = onReset
    )
}

@Composable
public fun MoreIcon(enabled: Boolean = true, onReset: () -> Unit) {
    DragonIconButton(
        icon = R.drawable.more_vert,
        contentDescription = R.string.more,
        enabled = enabled,
        onClick = onReset
    )
}