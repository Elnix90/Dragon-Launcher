package org.elnix.dragonlauncher.common.utils

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionName

object VersionsUtils {
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
}


@Composable
fun rememberVersionCode(): Int {
    val ctx = LocalContext.current
    return remember { ctx.getVersionCode() }
}

@Composable
fun rememberVersionName(): String {
    val ctx = LocalContext.current
    return remember { ctx.getVersionName() }
}
