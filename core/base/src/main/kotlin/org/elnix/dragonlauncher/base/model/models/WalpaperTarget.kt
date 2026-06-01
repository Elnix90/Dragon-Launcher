package org.elnix.dragonlauncher.base.model.models

import android.app.WallpaperManager

enum class WallpaperTarget(val flags: Int) {
    Home(WallpaperManager.FLAG_SYSTEM),
    Lock(WallpaperManager.FLAG_LOCK),
    Both(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
}
