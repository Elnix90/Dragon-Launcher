package org.elnix.dragonlauncher.common.utils

import androidx.activity.ComponentActivity

public object LifecycleUtils {
    public fun closeApp(activity: ComponentActivity) {
        activity.finishAffinity()
    }
}