package org.elnix.dragonlauncher.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap

class WallpaperHelper(private val context: Context) {

    fun setWallpaper(bitmap: Bitmap, flags: Int = WallpaperManager.FLAG_SYSTEM): Boolean {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (flags and WallpaperManager.FLAG_SYSTEM != 0) {
                    wallpaperManager.clear(WallpaperManager.FLAG_SYSTEM)
                }
                if (flags and WallpaperManager.FLAG_LOCK != 0) {
                    wallpaperManager.clear(WallpaperManager.FLAG_LOCK)
                }
            }

            if (flags and WallpaperManager.FLAG_SYSTEM != 0) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
            }
            if (flags and WallpaperManager.FLAG_LOCK != 0) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // Unused cause I found out that getting the current wallpaper no android is very protected, so il only use my app data
//    fun getCurrentWallpaper(): Drawable? {
//        return try {
//            val wallpaperManager = WallpaperManager.getInstance(context)
//            wallpaperManager.drawable
//        } catch (e: Exception) {
//            Log.e("Wallpaper", "Failed to get wallpaper", e)
//            null
//        }
//    }

   fun createPlainWallpaperBitmap(context: Context, color: Color): Bitmap {
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        // Create plain bitmap matching screen size
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        val bgColor = color.toArgb()
        canvas.drawColor(bgColor)

        return bitmap
    }
}
