package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup

@Composable
public fun DebugZone(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(visible) {
        Column(Modifier.settingsGroup()) {
            CompositionLocalProvider(
                LocalContentColor provides Color.White,
                LocalTextStyle provides TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 12.sp
                ),
                content = content
            )
        }
    }
}

@Composable
public fun DebugZone(setting: BooleanSettingObject, content: @Composable () -> Unit) {
    val isVisible by setting.asState()
    DebugZone(isVisible, content)
}

