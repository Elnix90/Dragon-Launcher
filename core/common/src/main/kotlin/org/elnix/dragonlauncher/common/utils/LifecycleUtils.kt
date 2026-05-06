package org.elnix.dragonlauncher.common.utils

import androidx.activity.ComponentActivity
import kotlinx.coroutines.delay

object LifecycleUtils {

    fun closeApp(activity: ComponentActivity) {
        activity.finishAffinity()
    }

    suspend fun waitASec() {
        delay(1000L)
    }
}