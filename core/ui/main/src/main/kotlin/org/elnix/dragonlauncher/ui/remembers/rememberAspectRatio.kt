package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberAspectRatio(): Float {
    val configuration = LocalConfiguration.current

    return remember {
        val width = configuration.screenWidthDp
        val height = configuration.screenHeightDp

        width.toFloat() / height.toFloat()
    }
}