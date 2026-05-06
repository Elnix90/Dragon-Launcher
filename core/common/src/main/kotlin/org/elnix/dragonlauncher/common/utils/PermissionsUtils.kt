package org.elnix.dragonlauncher.common.utils

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import org.elnix.dragonlauncher.common.messyfolder.Constants.PackageNameLists.systemLaunchers

object PermissionsUtils {


    /**
     * Check if an app is installed by package name.
     */
    fun Context.isAppInstalled(packageName: String): Boolean {
        return packageManager.getLaunchIntentForPackage(packageName) != null
    }



//fun hasAllFilesAccess(context: Context): Boolean {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        // Android 11+
//        Environment.isExternalStorageManager()
//    } else {
//        // Android 10 and below (uses old READ/WRITE)
//        ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.READ_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//}
//
//// Request function (requires an Activity or the activity result launcher equivalent)
//fun requestAllFilesAccess(activity: Activity) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        // Intent to redirect the user to the "All files access" setting
//        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//        val uri = Uri.fromParts("package", activity.packageName, null)
//        intent.data = uri
//        activity.startActivity(intent)
//    }
//    // For older APIs, the standard permission request dialog is used.
//}


    fun Context.hasUriReadPermission(uri: Uri): Boolean {
        val perms = contentResolver.persistedUriPermissions
        return perms.any { it.uri == uri && it.isReadPermission }
    }

    fun Context.hasUriReadWritePermission(uri: Uri): Boolean {
        val perms = contentResolver.persistedUriPermissions
        return perms.any { perm ->
            perm.uri == uri &&
                    perm.isReadPermission &&
                    perm.isWritePermission
        }
    }

    fun hasUsageStatsPermission(ctx: Context): Boolean {
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val uid = Process.myUid()
        val pkg = ctx.packageName

        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                uid,
                pkg
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                uid,
                pkg
            )
        }

        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun detectSystemLauncher(ctx: Context): String? {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Method 1: Check foreground task (most reliable)
        @Suppress("DEPRECATION")
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
            } catch (_: Exception) {
                // Package not installed
            }
        }

        return null  // No system launcher detected
    }
}