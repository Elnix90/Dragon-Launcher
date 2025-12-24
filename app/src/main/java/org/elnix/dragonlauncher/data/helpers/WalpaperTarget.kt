package org.elnix.dragonlauncher.data.helpers

import android.app.WallpaperManager

enum class WallpaperTarget(val flags: Int) {
    HOME(WallpaperManager.FLAG_SYSTEM),
    LOCK(WallpaperManager.FLAG_LOCK),
    BOTH(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK),
    JUST_DRAGON(-1)
}
