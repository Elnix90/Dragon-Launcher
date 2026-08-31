package org.elnix.dragonlauncher.base.model.models

import android.app.WallpaperManager

public enum class WallpaperTarget(
    public val flags: Int
) {
    Home(WallpaperManager.FLAG_SYSTEM),
    Lock(WallpaperManager.FLAG_LOCK),
    Both(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
}
