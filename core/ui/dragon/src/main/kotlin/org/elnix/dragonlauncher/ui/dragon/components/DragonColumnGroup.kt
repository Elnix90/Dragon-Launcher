package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup

@Composable
public fun DragonColumnGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
): Unit = CompositionLocalProvider(
    LocalContentColor provides MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.settingsGroup(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}


@Composable
public fun DragonColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
): Unit = CompositionLocalProvider(
    LocalContentColor provides MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
