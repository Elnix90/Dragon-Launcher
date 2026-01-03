package org.elnix.dragonlauncher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun isValidTimeFormat(formatter: String): Boolean = try {
    val timeFormatter = DateTimeFormatter.ofPattern(formatter)
    val now = LocalTime.now()
    now.format(timeFormatter)
    true
} catch (e: Exception) {
    println("❌ Time format validation failed: '$formatter' -> ${e.message}")
    false
}

@Composable
fun isValidDateFormat(formatter: String): Boolean = try {
    val dateFormatter = DateTimeFormatter.ofPattern(formatter)
    val today = LocalDate.now()
    today.format(dateFormatter)
    true
} catch (e: Exception) {
    println("❌ Date format validation failed: '$formatter' -> ${e.message}")
    false
}


fun detectSystemLauncher(ctx: Context): String? {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // Method 1: Check foreground task (most reliable)
    val task = am.getRunningTasks(1)?.firstOrNull()
    val topPkg = task?.topActivity?.packageName
    if (systemLaunchers.contains(topPkg)) {
        return topPkg
    }

    // Method 2: Query intent resolvers (default home)
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        addCategory(Intent.CATEGORY_DEFAULT)
    }

    val resolveInfos = ctx.packageManager.queryIntentActivities(homeIntent, 0)
    for (resolveInfo in resolveInfos) {
        val pkg = resolveInfo.activityInfo.packageName
        if (systemLaunchers.contains(pkg)) {
            return pkg
        }
    }

    // Method 3: Check enabled components (backup)
    val pm = ctx.packageManager
    for (sysPkg in systemLaunchers) {
        try {
            pm.getPackageInfo(sysPkg, 0)
            val launcherActivity = pm.queryIntentActivities(homeIntent, 0)
                .find { it.activityInfo.packageName == sysPkg }
            if (launcherActivity != null) return sysPkg
        } catch (e: Exception) {
            // Package not installed
        }
    }

    return null  // No system launcher detected
}
