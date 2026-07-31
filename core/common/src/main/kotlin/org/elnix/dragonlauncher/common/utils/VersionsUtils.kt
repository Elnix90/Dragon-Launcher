package org.elnix.dragonlauncher.common.utils

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionName

public object VersionsUtils {
    private var _versionCode: Int? = null

    /**
     * @return the current app version code (e.g. `46`)
     */
    public fun Context.getVersionCode(): Int =
        _versionCode ?: run {
            _versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionCode
            }
            _versionCode!!
        }


    private var _versionName: String? = null

    /**
     * @return the current app version name (e.g. `2.7.0-Glowel`)
     */
    public fun Context.getVersionName(): String =
        _versionName ?: run {
            _versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
            _versionName!!
        }

    /**
     * @return the current app version name and code formatted (e.g. `2.7.0-Glowel (46)`)
     */
    public fun Context.getVersionNameAndCode(): String =
        "${getVersionName()} (${getVersionCode()})"

    /**
     * Checks if the current build is a `beta` version flavor
     *
     * @return [Boolean] whether the build is a `beta` or not
     */
    public fun Context.isBetaVersion(): Boolean =
        getVersionName().contains("beta")

    /**
     * Checks if the current build is a `debug` version flavor
     *
     * @return [Boolean] whether the build is a `debug` or not
     */
    public fun Context.isDebugVersion(): Boolean =
        getVersionName().contains("debug")
}


// TODO remove these as the remembered work is in the VersionsUtils object
@Composable
public fun rememberVersionCode(): State<Int> {
    val ctx = LocalContext.current
    return remember { mutableIntStateOf(ctx.getVersionCode()) }
}

@Composable
public fun rememberVersionName(): State<String> {
    val ctx = LocalContext.current
    return remember { mutableStateOf(ctx.getVersionName()) }
}