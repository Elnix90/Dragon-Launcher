package org.elnix.dragonlauncher.base.utils

import android.content.Context
import android.os.Build

public object VersionsUtils {
    private var _versionCode: Int? = null
    private var _versionNumber: String? = null
    private var _codeName: String? = null
    private var _buildType: String? = null

    /**
     * Reads from the [android.content.pm.PackageManager] and splits the version.
     *
     * This only works because I strictly controls how the version is formatted in the first place.
     * if a single version isn't formatted correctly this would throw instantly
     */
    private fun Context.load() {
        val fullVersion = packageManager.getPackageInfo(packageName, 0).versionName!!

        _versionNumber = fullVersion.substringBefore(' ')
        _codeName = fullVersion.substringAfter('(').substringBefore(')')
        _buildType = if ('-' in fullVersion) fullVersion.substringAfterLast('-') else "release"

        _versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionCode
            }
        _versionCode!!
    }

    /**
     * @return the current app version code (e.g. `46`)
     */
    public fun Context.getVersionCode(): Int =
        _versionCode ?: run {
            load()
            _versionCode!!
        }

    /**
     * @return the current app code name, (e.g. `Labyrinth`)
     */
    public fun Context.getCodeName(): String =
        _codeName ?: run {
            load()
            _codeName!!
        }

    /**
     * @return the current app version number, (e.g. `4.1.1`)
     */
    public fun Context.getVersionNumber(): String =
        _versionNumber ?: run {
            load()
            _versionNumber!!
        }

    /**
     * @return the current build type (e.g. `release`, `debug` or `beta`)
     */
    public fun Context.getBuildType(): String =
        _buildType ?: run {
            load()
            _buildType!!
        }

    /**
     * Checks if the current build is a `beta` version flavor
     *
     * @return [Boolean] whether the build is a `beta` or not
     */
    public fun Context.isBetaVersion(): Boolean = getBuildType() == "beta"
}
