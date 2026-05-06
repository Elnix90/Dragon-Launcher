package org.elnix.dragonlauncher.common.messyfolder

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.logging.logE

/**
 * Show a toast message with flexible input types
 * @param message Can be a String, StringRes Int, or null
 * @param duration Toast duration (LENGTH_SHORT or LENGTH_LONG)
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

/**
 * Returns `true` if this string represents an empty JSON object.
 *
 * The value is considered valid when:
 * - It is not blank (after trimming whitespace).
 * - It is not equal to `"{}"` (an empty JSON object).
 *
 * This is a lightweight structural check and does not validate
 * whether the string is well-formed JSON.
 */
val String?.isBlankJson: Boolean
    get() {
        if (this == null) return true
        val trimmed = trim()
        return trimmed.isEmpty() || trimmed == "{}" || trimmed == "[]"
    }


/**
 * Returns `true` if this string represents a non-empty JSON object.
 *
 * The value is considered valid when:
 * - It is not blank (after trimming whitespace).
 * - It is not equal to `"{}"` (an empty JSON object).
 *
 * This is a lightweight structural check and does not validate
 * whether the string is well-formed JSON.
 */
val String?.isNotBlankJson: Boolean
    get() = !isBlankJson


//fun <T> SnapshotStateList<T>.move(from: Int, to: Int) {
//    if (from == to) return
//    if (from in 0 until size && to in 0 until size) {
//        add(to, removeAt(from))
//    }
//}

fun <E> MutableSet<E>.addOrRemove(element: E) {
    if (contains(element)) {
        remove(element)
    } else add(element)
}
