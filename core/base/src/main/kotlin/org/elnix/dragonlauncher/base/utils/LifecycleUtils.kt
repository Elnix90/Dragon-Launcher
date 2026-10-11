package org.elnix.dragonlauncher.base.utils

import androidx.activity.ComponentActivity
import kotlin.system.exitProcess

object LifecycleUtils {
	fun closeApp(activity: ComponentActivity) {
		activity.finishAffinity()
		exitProcess(0)
	}
}
