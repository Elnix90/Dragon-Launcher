package org.elnix.dragonlauncher.base.utils

import android.content.Context
import android.os.Build

object VersionsUtils {
	private var _versionCode: Int? = null
	private var _versionNumber: String? = null
	private var _codeName: String? = null
	private var _buildType: BuildType? = null

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

		val buildTypeString = if ('-' in fullVersion) fullVersion.substringAfterLast('-') else "release"
		_buildType = BuildType.valueOf(buildTypeString)

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
	fun Context.getVersionCode(): Int =
		_versionCode ?: run {
			load()
			_versionCode!!
		}

	/**
	 * @return the current app code name, (e.g. `Labyrinth`)
	 */
	fun Context.getCodeName(): String =
		_codeName ?: run {
			load()
			_codeName!!
		}

	/**
	 * @return the current app version number, (e.g. `4.1.1`)
	 */
	fun Context.getVersionNumber(): String =
		_versionNumber ?: run {
			load()
			_versionNumber!!
		}

	/**
	 * @return the current [BuildType]
	 */
	fun Context.getBuildType(): BuildType =
		_buildType ?: run {
			load()
			_buildType!!
		}

	/**
	 * Checks if the current build is a `beta` version flavor
	 *
	 * @return [Boolean] whether the build is a `beta` or not
	 */
	fun Context.isBetaVersion(): Boolean = getBuildType() == BuildType.beta

	@Suppress("EnumEntryName")
	enum class BuildType {
		/**
		 * F-Droid or any freely distributed build
		 */
		release,

		/**
		 * Unminified build, for testing only
		 */
		debug,

		/**
		 * Minified build, but with another version name and package, to be installed alongside the main app
		 */
		beta,

		/**
		 * The Google Play Store build.
		 * As I'm writing this I dunno whether I'll limit what it can do or make it paid, but for sure, it won't be the same as [release]
		 */
		playstore
	}
}
