package org.elnix.dragonlauncher.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.widget.Toast
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