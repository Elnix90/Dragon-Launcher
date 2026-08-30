package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.runtime.Composable
import org.elnix.dragonlauncher.i18n.R

@Composable
fun ResetIcon(enabled: Boolean = true, onReset: () -> Unit) {
    DragonIconButton(
        icon = R.drawable.reset,
        contentDescription = R.string.reset,
        enabled = enabled,
        onClick = onReset
    )
}

@Composable
fun MoreIcon(enabled: Boolean = true, onReset: () -> Unit) {
    DragonIconButton(
        icon = R.drawable.more_vert,
        contentDescription = R.string.more,
        enabled = enabled,
        onClick = onReset
    )
}

@Composable
fun CopyIcon(enabled: Boolean = true, onCopy: () -> Unit) {
    DragonIconButton(
        icon = R.drawable.copy,
        contentDescription = R.string.copy,
        enabled = enabled,
        onClick = onCopy
    )
}
