package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import org.elnix.dragonlauncher.base.theme.LocalExtraColors

@Composable
public fun rememberPointTextStyle(): TextStyle {
    val labelSmall = MaterialTheme.typography.labelSmall
    val extraColors = LocalExtraColors.current
    return remember(labelSmall, extraColors) {
        labelSmall.copy(color = extraColors.shapes)
    }
}