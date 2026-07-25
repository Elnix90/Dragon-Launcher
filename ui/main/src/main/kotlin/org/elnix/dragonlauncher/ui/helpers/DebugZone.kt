package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup

@Composable
fun DebugZone(visible: Boolean, content: @Composable () -> Unit) {
    if (!visible) return

    Column(
        modifier = Modifier
            .padding(10.dp)
            .settingsGroup()
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.White,
            LocalTextStyle provides MaterialTheme.typography.labelSmall,
            content = content
        )
    }
}

@Composable
fun DebugZone(setting: BooleanSettingObject, content: @Composable () -> Unit) {
    val isVisible by setting.asState()
    DebugZone(isVisible, content)
}

