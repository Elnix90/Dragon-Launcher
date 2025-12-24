package org.elnix.dragonlauncher.utils

import org.elnix.dragonlauncher.ui.SETTINGS

const val obtainiumPackageName = "dev.imranr.obtainium.fdroid"


/** List of routes that the routes killer ignores when the user leave the app for too long, usually files pickers */
val ignoredReturnRoutes = listOf(
    SETTINGS.BACKUP,
    SETTINGS.WALLPAPER
)
