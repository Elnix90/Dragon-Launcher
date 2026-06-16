package org.elnix.dragonlauncher.ktx

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import org.elnix.dragonlauncher.logging.SECURITY_SERVICE
import org.elnix.dragonlauncher.logging.TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE


/**
 * Show a toast message with flexible input types
 * @param message Can be a String, StringRes Int, or null
 * @param duration Toast duration ([Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG])
 */
fun Context.showToast(
    message: Any?,
    duration: Int = Toast.LENGTH_SHORT
) {
    val context = this
    val handler = Handler(Looper.getMainLooper())
    handler.post {
        try {
            when (message) {
                is String -> {
                    if (message.isNotBlank()) {
                        Toast.makeText(context, message, duration).show()
                    }
                }

                is Int -> {
                    Toast.makeText(context, message, duration).show()
                }

                else -> {
                    // Null or unsupported type, do nothing
                }
            }
        } catch (e: Exception) {
            logE(TAG, e) { "Error while showing toast" }
        }
    }
}


fun Context.openUrl(url: String) {
    if (url.isEmpty()) return
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = url.toUri()
    startActivity(intent)
}


fun Context.openSearch(query: String) {
    val intent = Intent(Intent.ACTION_WEB_SEARCH)
    intent.putExtra(SearchManager.QUERY, query)
    startActivity(intent)
}

fun Context.expandQuickActionsDrawer() {
    try {
        //  (Android 12+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val statusBarManager = context.getSystemService(Context.STATUS_BAR_SERVICE) as StatusBarManager
//            statusBarManager.expandNotificationsPanel()
//            return
//        }

        // Fall back -> reflection for older versions
        val statusBarService = getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val method = statusBarManager.getMethod("expandNotificationsPanel")
        method.invoke(statusBarService)
    } catch (_: Exception) {
        // If all else fails, try to use the notification intent
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }
}


fun Context.getFilePathFromUri(uri: Uri): String {
    // 1. Try SAF document path reconstruction
    if (DocumentsContract.isDocumentUri(this, uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":")
        if (split.size == 2) {
            val type = split[0]
            val subPath = split[1]

            // Internal storage (primary)
            if (type.equals("primary", ignoreCase = true)) {
                return "/storage/emulated/0/$subPath"
            }
        }
    }

    // 2. If not from primary storage: fall back to the display name
    val name = this.getUriDisplayName(uri)
    if (name != null) return name

    // 3. Last fallback: last path segment
    return uri.lastPathSegment ?: "Unknown file"
}

private fun Context.getUriDisplayName(uri: Uri): String? {
    return try {
        this.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else null
        }
    } catch (_: Exception) {
        null
    }
}


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

//fun Context.hasUsageStatsPermission(): Boolean {
//    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//    val uid = Process.myUid()
//    val pkg = this.packageName
//
//    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        appOps.unsafeCheckOpNoThrow(
//            AppOpsManager.OPSTR_GET_USAGE_STATS,
//            uid,
//            pkg
//        )
//    } else {
//        @Suppress("DEPRECATION")
//        appOps.checkOpNoThrow(
//            AppOpsManager.OPSTR_GET_USAGE_STATS,
//            uid,
//            pkg
//        )
//    }
//
//    return mode == AppOpsManager.MODE_ALLOWED
//}

val Context.dp: Float
    get() = resources.displayMetrics.density


val Context.sp: Float
    get() = resources.displayMetrics.scaledDensity

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}


fun Context.tryStartActivity(intent: Intent, bundle: Bundle? = null): Boolean {
    return try {
        startActivity(intent, bundle)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}


fun Context.getInstallSource(
    packageName: String
): InstallSourceInfoCompat {
    val pm = this.packageManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val installSourceInfo = pm.getInstallSourceInfo(packageName)
        return InstallSourceInfoCompat(
            originatingPackageName = installSourceInfo.originatingPackageName,
            initiatingPackageName = installSourceInfo.initiatingPackageName,
            installingPackageName = installSourceInfo.installingPackageName,
        )
    } else {
        val installerPackageName = pm.getInstallerPackageName(packageName)
        return InstallSourceInfoCompat(
            originatingPackageName = installerPackageName,
            initiatingPackageName = installerPackageName,
            installingPackageName = installerPackageName
        )
    }
}

/**
 * Walks up the Context wrapper chain to find the hosting FragmentActivity.
 * Compose's `LocalContext.current` may be wrapped by ContextThemeWrapper or similar.
 */
fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx: Context? = this
    var depth = 0
    while (ctx != null && depth < 20) { // Prevent infinite loops
        logD(SECURITY_SERVICE) { "findFragmentActivity: depth=$depth, ctx=${ctx::class.simpleName}" }
        when (ctx) {
            is FragmentActivity -> {
                logD(SECURITY_SERVICE) { "Found FragmentActivity at depth $depth" }
                return ctx
            }

            is ContextWrapper -> ctx = ctx.baseContext
            else -> {
                logD(SECURITY_SERVICE) { "Context is not ContextWrapper, cannot unwrap further" }
                return null
            }
        }
        depth++
    }
    logD(SECURITY_SERVICE) { "findFragmentActivity failed after $depth iterations" }
    return null
}

fun Context.openDefaultLauncherSettings() {
    tryStartActivity(Intent(Settings.ACTION_HOME_SETTINGS))
}

data class InstallSourceInfoCompat(
    val originatingPackageName: String?,
    val initiatingPackageName: String?,
    val installingPackageName: String?
)