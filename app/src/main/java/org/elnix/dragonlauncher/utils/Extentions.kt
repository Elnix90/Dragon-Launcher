package org.elnix.dragonlauncher.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import org.elnix.dragonlauncher.R

/**
 * Functions from https://github.com/mlm-games/CCLauncher
 * (https://github.com/mlm-games/CCLauncher/blob/compose/app/src/main/java/app/cclauncher/helper/Extensions.kt)
 */


/**
 * Show a toast message with flexible input types
 * @param message Can be a String, StringRes Int, or null
 * @param duration Toast duration (LENGTH_SHORT or LENGTH_LONG)
 */
fun Context.showToast(
    message: Any?,
    duration: Int = Toast.LENGTH_SHORT
) {
    when (message) {
        is String -> {
            if (message.isNotBlank()) {
                Toast.makeText(this, message, duration).show()
            }
        }
        is Int -> {
            try {
                Toast.makeText(this, getString(message), duration).show()
            } catch (_: Exception) {
                // Invalid resource ID, ignore
            }
        }
        else -> {
            // Null or unsupported type, do nothing
        }
    }
}


fun Context.copyToClipboard(text: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(getString(R.string.app_name), text)
    clipboardManager.setPrimaryClip(clipData)
    showToast("")
}



fun Context.openUrl(url: String) {
    if (url.isEmpty()) return
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = url.toUri()
    startActivity(intent)
}


val Context.isDefaultLauncher: Boolean
    get() {
        // 1. Create the Intent that represents the Home action
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        // 2. Resolve the activity that would handle this intent (the default Home activity)
        val resolveInfo: ResolveInfo? = packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY // Only consider activities that are a full match (including categories)
        )

        // 3. Check if the resolved activity belongs to your application's package
        return resolveInfo?.activityInfo?.packageName == packageName
    }



//
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



fun Context.hasUriPermission(uri: Uri): Boolean {
    val perms = contentResolver.persistedUriPermissions
    return perms.any { it.uri == uri && it.isReadPermission }
}
