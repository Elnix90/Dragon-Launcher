package org.elnix.dragonlauncher.ui.drawer

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.elnix.dragonlauncher.data.SwipeActionSerializable

data class AppModel(
    val name: String,
    val packageName: String,
    val isSystem: Boolean,
) {
    val action = SwipeActionSerializable.LaunchApp(packageName)
}
