package org.elnix.dragonlauncher.ui.helpers.wallpaper

import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer

@Composable
fun WallpaperDim(dimAmount: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(dimAmount))
    )
}


@Composable
fun CustomDim(
    customDim: MainScreenLayer.CustomDim
) {
    val bgColor = MaterialTheme.colorScheme.background
    val color = remember {
        Animatable(Color.Transparent)
    }

    LaunchedEffect(Unit) {
        color.animateTo(bgColor.copy(customDim.dimAmount))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.value)
    )
}