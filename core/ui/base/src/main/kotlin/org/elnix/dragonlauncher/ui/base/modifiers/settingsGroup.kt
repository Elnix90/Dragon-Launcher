package org.elnix.dragonlauncher.ui.base.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.util.ColorUtils.semiTransparentIfDisabled


@Composable
fun Modifier.settingsGroup(
    clickModifier: Modifier? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    enabled: Boolean = true
): Modifier {
    return this
        .clip(MaterialTheme.shapes.large)
        .background(backgroundColor.semiTransparentIfDisabled(enabled))
        .then(clickModifier ?: this)
        .padding(10.dp)
}
