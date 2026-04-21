package org.elnix.dragonlauncher.common.utils

import android.annotation.SuppressLint
import android.app.SearchManager
import android.app.role.RoleManager
import android.bluetooth.BluetoothManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.utils.Constants.Logging.CONTEXT_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.STATUS_BAR_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.utils.Constants.PackageNameLists.knownClockPackages
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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


/**
 * Shares content via Intent.ACTION_SEND. Handles both file-based and text-based sharing
 * with automatic fallback if FileProvider is not configured.
 *
 * @param uri The file URI to share (optional). If null, falls back to text sharing.
 * @param text The text content to share as fallback or direct share.
 * @param subject The subject line for the share intent.
 * @param mimeType The MIME type (default: "text/plain").
 * @param chooserTitle The title shown in the share chooser.
 */
fun Context.shareContent(
    uri: Uri? = null,
    text: String? = null,
    subject: String = "Share",
    mimeType: String = "text/plain",
    chooserTitle: String = "Share"
) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType

            if (uri != null) {
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (text != null) {
                putExtra(Intent.EXTRA_TEXT, text)
            }

            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        startActivity(Intent.createChooser(shareIntent, chooserTitle))

    } catch (e: Exception) {
        logE(CONTEXT_TAG, e) { "Failed to share content" }
        Toast.makeText(this, "Failed to share", Toast.LENGTH_SHORT).show()
    }
}



/**
 * Creates a shareable file from the given text, using the app's cache directory.
 *
 * @param text The text content to save.
 * @param filename The name of the file to create.
 * @return A pair of (File in cache, FileProvider Uri) or null if creation failed.
 */
fun Context.createShareableTextFile(
    text: String,
    filename: String = "share_${System.currentTimeMillis()}.txt"
): Pair<File, Uri>? {
    return try {
        val shareFile = File(cacheDir, filename)
        shareFile.writeText(text)

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            shareFile
        )

        Pair(shareFile, uri)
    } catch (e: Exception) {
        logE(CONTEXT_TAG, e) { "Failed to create shareable text file" }
        null
    }
}

/**
 * Creates a shareable file from the given file, using the app's cache directory.
 *
 * @param sourceFile The file to copy to cache.
 * @return A pair of (File in cache, FileProvider Uri) or null if creation failed.
 */
fun Context.createShareableFile(sourceFile: File): Pair<File, Uri>? {
    return try {
        val cacheDir = cacheDir
        val shareFile = File(cacheDir, sourceFile.name)
        sourceFile.copyTo(shareFile, overwrite = true)

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            shareFile
        )

        Pair(shareFile, uri)
    } catch (e: Exception) {
        logE(CONTEXT_TAG, e) { "Failed to create shareable file" }
        null
    }
}

fun Context.copyToClipboard(text: String, maxSize: Int = 500 * 1024) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    if (text.length > maxSize) {
        logW(CONTEXT_TAG) { "Text too large for clipboard (${text.length} bytes), creating file to share" }

        val (_, uri) = createShareableTextFile(text, "clipboard_${System.currentTimeMillis()}.txt")
            ?: run {
                showToast("Failed to save text to file")
                return
            }

        shareContent(
            uri = uri,
            subject = "Clipboard Content",
            chooserTitle = "Share Text"
        )
        showToast("Text too large, opening share intent")
        return
    }

    try {
        val clipData = ClipData.newPlainText(getString(R.string.app_name), text)
        clipboardManager.setPrimaryClip(clipData)
        logD(CONTEXT_TAG) { "Copied to clipboard" }
        showToast("Copied to clipboard!")
    } catch (e: Exception) {
        logE(CONTEXT_TAG, e) { "Failed to copy to clipboard" }
        showToast("Failed to copy to clipboard")
    }
}


fun Context.pasteClipboard(): String? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0).coerceToText(this)?.toString()
}


fun Context.openUrl(url: String) {
    if (url.isEmpty()) return
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = url.toUri()
    startActivity(intent)
}


/**
 * Check if an app is installed by package name.
 */
fun Context.isAppInstalled(packageName: String): Boolean {
    return packageManager.getLaunchIntentForPackage(packageName) != null
}


fun Context.isBluetoothEnabled(): Boolean {
    val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return bluetoothManager.adapter?.isEnabled == true
}

//fun Context.isHotspotEnabled(): Boolean =
//    Settings.Global.getInt(contentResolver, "wifi_ap_state", 0) == 13

fun Context.isHotspotEnabled(): Boolean {
    val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
    return try {
        val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
        method.isAccessible = true
        method.invoke(wifiManager) as Boolean
    } catch (e: Exception) {
        showToast("Error fetching hotspot state: $e")
        logE(STATUS_BAR_TAG, e) { "Security Exception fetching hotspot state!"}

        // Fallback to settings
        Settings.Global.getInt(contentResolver, "wifi_ap_state", 0) == 13
    }
}

fun Context.isWifiEnabled(): Boolean {
    return try {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
         wifiManager.isWifiEnabled
    } catch (e: SecurityException) {
        showToast("Error fetching internet state: $e")
        logE(STATUS_BAR_TAG, e) { "Security Exception fetching wifi state!"}
        false
    }
}

@SuppressLint("MissingPermission")
fun Context.getMobileDataStatus(): Pair<Boolean, String> {

    val resolver = contentResolver
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    /*  ─────────────  Mobile data status  ─────────────  */
    // 1. Check if mobile data is enabled (check multiple SIMs)
    val mobileDataEnabled = try {
        Settings.Global.getInt(resolver, "mobile_data", 0) == 1 ||
                Settings.Global.getInt(resolver, "mobile_data1", 0) == 1 ||
                Settings.Global.getInt(resolver, "mobile_data2", 0) == 1
    } catch (_: Exception) {
        true // Default to enabled if unable to access
    }

    if (!mobileDataEnabled) return false to "Data OFF"

    // 2. Get active cellular network + signal
    val activeNetwork = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

    if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
        // For now, just return network type without signal strength access might require additional permissions
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
        val networkType = try {
            telephonyManager.dataNetworkType
        } catch (_: Exception) {
            android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN
        }

        val typeStr = when (networkType) {
            android.telephony.TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            20 -> "5G" // TelephonyManager.NETWORK_TYPE_NR = 20
            android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA, android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"
            else -> "2G"
        }

        val isRoaming = try {
            telephonyManager.isNetworkRoaming
        } catch (_: Exception) {
            false
        }

        return true to (if (isRoaming) "$typeStr (Roaming)" else typeStr)
    }

    return true to "Data ON"
}

fun Context.isVpnEnabled(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return connectivityManager.allNetworks.any { network ->
        connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    }
}

fun Context.isAirplaneMode(): Boolean {
    return Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
}
val Context.isDefaultLauncher: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        } else {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }

            val resolveInfo = packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )

            resolveInfo?.activityInfo?.packageName == packageName
        }
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

fun Context.openAlarmApp2(): Boolean {
    val pm = this.packageManager

    // 1. Official alarm UI
    val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (alarmIntent.resolveActivity(pm) != null) {
        startActivity(alarmIntent)
        return true
    }

    // 2. Clock apps that declare alarm/clock actions
    val alarmLikeIntents = listOf(
        Intent(AlarmClock.ACTION_SET_ALARM),
        Intent("android.intent.action.SHOW_ALARMS"),
        Intent("android.intent.action.SHOW_ALARM")
    )

    val candidates = alarmLikeIntents
        .flatMap { base ->
            pm.queryIntentActivities(
                base,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        }
        .distinctBy { it.activityInfo.packageName to it.activityInfo.name }

    if (candidates.isNotEmpty()) {
        val best = candidates.first()
        startActivity(
            Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                this.component = ComponentName(
                    best.activityInfo.packageName,
                    best.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        return true
    }

    // 3. Launcher activities, filtered by known clock packages or name
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val launcherActivities = pm.queryIntentActivities(
        launcherIntent,
        PackageManager.MATCH_DEFAULT_ONLY
    )

    val fallback = launcherActivities.firstOrNull {
        val pkg = it.activityInfo.packageName
        pkg in knownClockPackages ||
                pkg
                    .contains("clock", ignoreCase = true) || it.loadLabel(pm).toString()
            .contains("clock", ignoreCase = true)
    }

    if (fallback != null) {
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                this.component = ComponentName(
                    fallback.activityInfo.packageName,
                    fallback.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        return true
    }

    return false
}

fun Context.openAlarmApp() {
    val pm = this.packageManager

    // Try official alarm actions in priority order
    listOf(
        AlarmClock.ACTION_SHOW_ALARMS,
        AlarmClock.ACTION_SET_ALARM,
        AlarmClock.ACTION_SET_TIMER
    ).forEach { action ->
        val intent = Intent(action).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent)
            return
        }
    }

    // Fallback, use the other function
    if (!this.openAlarmApp2()) return

    // No alarm-capable app found
    logD(TAG) { "No alarm app found" }
}


fun Context.openCalendar() {
    try {
        val calendarUri = CalendarContract.CONTENT_URI
            .buildUpon()
            .appendPath("time")
            .build()
        startActivity(Intent(Intent.ACTION_VIEW, calendarUri))
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            val intent = Intent(Intent.ACTION_MAIN).setClassName(
                this,
                "org.elnix.dragonlauncher.MainActivity"
            )
            intent.addCategory(Intent.CATEGORY_APP_CALENDAR)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * @return the current app version code (e.g. `46`)
 */
fun Context.getVersionCode(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0).versionCode
    }

/**
 * @return the current app version name (e.g. `2.7.0-Glowel`)
 */
fun Context.getVersionName(): String =
    packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"

/**
 * @return the current app version name and code formatted (e.g. `2.7.0-Glowel (46)`)
 */
fun Context.getVersionNameAndCode(): String =
    "${getVersionName()} (${getVersionCode()})"

/**
 * Checks if the current build is a beta version flavor
 * Actually, it just checks if the version name contains  `beta`
 *
 * @return [Boolean] whether the build is a beta or not
 */
fun Context.isBetaVersion(): Boolean =
    getVersionName().contains("beta")


/**
 * Takes a timestamp [Long] and return the formatted date in `MMM dd, yyyy HH:mm:ss` format
 * It uis used by the logs tab and the backup tab to format file dates in a readable output
 *
 * @return [String] the formatted [this] timestamp
 */
fun Long.formatDateTime(): String {
    return SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        .format(Date(this))
}

/**
 * Today - returns the today's date, formatted in the given format
 *
 * @param format the date format
 * @return [String] today's date
 */
fun today(format: String = "MMM dd, yyyy"): String =
    SimpleDateFormat(format, Locale.getDefault()).format(Date())


/**
 * Format duration
 * Takes a timestamp and format it into a duration in hours, minutes and seconds
 * Depending on the duration, the minutes and hours may or may not be displayed (e.g. if under 60 min, no hours)
 *
 * @return [String] the formatted duration
 */
fun Long.formatDuration(): String {
    return when {
        this >= 60 -> {
            val hours = this / 60
            val mins = this % 60
            if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
        }

        else -> "$this min"
    }
}

//fun Long.timeAgo(): String {
//    val seconds = (System.currentTimeMillis() - this) / 1000
//    return when {
//        seconds < 60 -> "${seconds}s ago"
//        seconds < 3600 -> "${seconds / 60}m ago"
//        seconds < 86400 -> "${seconds / 3600}h ago"
//        seconds < 2592000 -> "${seconds / 86400}d ago"
//        else -> "${seconds / 2592000}mo ago"
//    }
//}

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
