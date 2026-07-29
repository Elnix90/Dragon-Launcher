package org.elnix.dragonlauncher.common.utils

import androidx.activity.ComponentActivity
import kotlin.system.exitProcess

public object LifecycleUtils {
    public fun closeApp(activity: ComponentActivity) {
        activity.finishAffinity()
        exitProcess(0)
    }
}