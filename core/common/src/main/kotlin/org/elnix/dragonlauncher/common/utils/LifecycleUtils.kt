package org.elnix.dragonlauncher.common.utils

import androidx.activity.ComponentActivity

object LifecycleUtils {
    fun closeApp(activity: ComponentActivity) {
        activity.finishAffinity()
    }
}