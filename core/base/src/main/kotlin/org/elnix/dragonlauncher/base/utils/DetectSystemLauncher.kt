package org.elnix.dragonlauncher.base.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import org.elnix.dragonlauncher.base.Constants.PackageNameLists.systemLaunchers

public fun Context.detectSystemLauncher(): String? {
    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // Method 1: Check foreground task (most reliable)
    @Suppress("DEPRECATION")
    val task = am.getRunningTasks(1)?.firstOrNull()
    val topPkg = task?.topActivity?.packageName
    if (systemLaunchers.contains(topPkg)) {
        return topPkg
    }

    // Method 2: Query intent resolvers (default home)
    val homeIntent =
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

    val resolveInfos = this.packageManager.queryIntentActivities(homeIntent, 0)
    for (resolveInfo in resolveInfos) {
        val pkg = resolveInfo.activityInfo.packageName
        if (systemLaunchers.contains(pkg)) {
            return pkg
        }
    }

    // Method 3: Check enabled components (backup)
    val pm = this.packageManager
    for (sysPkg in systemLaunchers) {
        try {
            pm.getPackageInfo(sysPkg, 0)
            val launcherActivity =
                pm
                    .queryIntentActivities(homeIntent, 0)
                    .find { it.activityInfo.packageName == sysPkg }
            if (launcherActivity != null) return sysPkg
        } catch (_: Exception) {
            // Package not installed
        }
    }

    return null // No system launcher detected
}
