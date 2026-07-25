package org.elnix.dragonlauncher.ui.helpers.wallpaper

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun WallpaperBlur(radius: Int) {

    val ctx = LocalContext.current
    val density = LocalDensity.current

    val animatable = remember { Animatable(radius, Int.VectorConverter) }
    LaunchedEffect(radius) {
        animatable.animateTo(with(density) { radius.dp.toPx().toInt() }) {
            if (value > 0) {
                val windowAttributes = (ctx as Activity).window.attributes
                windowAttributes.flags =
                    windowAttributes.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                ctx.window.attributes = windowAttributes
                ctx.window.setBackgroundBlurRadius(value)
            } else {
                val windowAttributes = (ctx as Activity).window.attributes
                windowAttributes.flags =
                    windowAttributes.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
                ctx.window.attributes = windowAttributes
                ctx.window.setBackgroundBlurRadius(0)
            }
        }
    }
}
