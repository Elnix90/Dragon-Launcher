package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup

@Composable
fun DebugZone(setting: BooleanSettingObject, content: @Composable () -> Unit) {

    val isVisible by setting.asState()

    AnimatedVisibility(isVisible) {
        DragonColumnGroup {
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

