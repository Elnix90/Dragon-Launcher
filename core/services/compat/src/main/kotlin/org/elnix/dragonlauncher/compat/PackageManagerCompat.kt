package org.elnix.dragonlauncher.compat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process.myUserHandle
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import org.elnix.dragonlauncher.APPS_TAG
import org.elnix.dragonlauncher.ICONS_TAG
import org.elnix.dragonlauncher.PM_COMPAT_TAG
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.util.ImageUtils.loadDrawableAsBitmap
import org.elnix.dragonlauncher.i18n.R

interface PackageManagerCompat {
	/**
	 * Get ALL apps (launchable + system + hidden)
	 */
	fun getAllApplications(): List<ApplicationInfo>

	/**
	 * Get only launchable apps
	 */
	fun getLaunchableApps(): List<LauncherActivityInfo>

	/**
	 * Lists every launcher activity of every app for the given [profiles], merging
	 * [LauncherApps.getActivityList] (valid for every profile) with a raw
	 * [PackageManager.queryIntentActivities] query for the current user.
	 *
	 * The second source is needed because some ROMs (e.g. MIUI/HyperOS)
	 * filter [LauncherApps] down to one activity per package, while
	 * a plain PackageManager query still
	 * lists every launcher activity. Sorted by package name, then label.
	 */
	fun getLauncherActivities(profiles: List<Profile>): List<LauncherActivity>

	fun isSystemApp(appInfo: ApplicationInfo): Boolean

	fun getAppIcon(packageName: String, userId: Int, isPrivate: Boolean = false): Drawable

	fun getResourcesForApplication(packageName: String): Resources

	fun queryAppShortcuts(packageName: String): List<ShortcutInfo>

	fun launchShortcut(packageName: String, id: String)

	fun loadShortcutIcon(packageName: String, shortcutId: String, sizePx: Int): Bitmap?
}

internal class PackageManagerCompatImpl(
	private val ctx: Context
) : PackageManagerCompat {
	private val pm = ctx.packageManager
	private val launcherApps = ctx.getSystemService(LauncherApps::class.java)!!
	private val userManager = ctx.getSystemService(UserManager::class.java)!!

	override fun isSystemApp(appInfo: ApplicationInfo): Boolean {
		val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
		val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
		return isSystem &&
			!isUpdatedSystem &&
			(appInfo.packageName.startsWith("com.android.") || appInfo.packageName.startsWith("android"))
	}

	/**
	 * Get ALL installed applications across all profiles
	 */
	override fun getAllApplications(): List<ApplicationInfo> {
		val result = mutableListOf<ApplicationInfo>()
		val seenPackages = mutableSetOf<String>()

		// TODO wtf why isn't the userHandle used?
		userManager.userProfiles.forEach { userHandle ->
			try {
				// Get all apps including hidden ones
				val apps =
					pm
						.getInstalledApplications(PackageManager.GET_META_DATA)
						.filter { it.enabled || (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 }

				apps.forEach { appInfo ->
					if (!seenPackages.contains(appInfo.packageName)) {
						result.add(appInfo)
						seenPackages.add(appInfo.packageName)
					}
				}
			} catch (e: Exception) {
				logE(PM_COMPAT_TAG, e) { "Error getting all applications" }
			}
		}
		return result
	}

	/**
	 * Get only launchable apps
	 */
	override fun getLaunchableApps(): List<LauncherActivityInfo> {
		val result = mutableListOf<LauncherActivityInfo>()
		val seenKeys = mutableSetOf<String>()

		userManager.userProfiles.forEach { userHandle ->
			try {
				val activities = launcherApps.getActivityList(null, userHandle) ?: emptyList()
				activities.forEach { activity ->
					val key = "${activity.componentName.packageName}_${userHandle.hashCode()}"
					if (!seenKeys.contains(key)) {
						result.add(activity)
						seenKeys.add(key)
					}
				}
			} catch (e: Exception) {
				logE(PM_COMPAT_TAG, e) { "Error getting launchable apps" }
			}
		}
		return result
	}

	override fun getLauncherActivities(profiles: List<Profile>): List<LauncherActivity> {
		val loaded = mutableListOf<LauncherActivity>()
		val seen = mutableSetOf<Pair<Profile, ComponentName>>()

		fun add(label: CharSequence?, componentName: ComponentName, profile: Profile) {
			if (!seen.add(profile to componentName)) return
			loaded +=
				LauncherActivity(
					componentName = componentName,
					label = label?.toString() ?: componentName.className,
					profile = profile
				)
		}

		// Primary source: LauncherApps, works for every profile
		profiles.forEach { profile ->
			try {
				launcherApps.getActivityList(null, profile.userHandle)?.forEach { info ->
					add(info.label, info.componentName, profile)
				}
			} catch (e: Exception) {
				logE(PM_COMPAT_TAG, e) { "Error getting launcher activities for ${profile.type}" }
			}
		}

		// Secondary source: raw PackageManager query for the current user
		val currentProfile = profiles.firstOrNull { it.userHandle == myUserHandle() }
		if (currentProfile != null) {
			listOf(
				Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
				Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_INFO)
			).forEach { intent ->
				try {
					pm.queryIntentActivities(intent, PackageManager.MATCH_ALL).forEach { resolveInfo ->
						val activityInfo = resolveInfo.activityInfo ?: return@forEach
						val componentName = ComponentName(activityInfo.packageName, activityInfo.name)
						add(resolveInfo.loadLabel(pm), componentName, currentProfile)
					}
				} catch (e: Exception) {
					logE(PM_COMPAT_TAG, e) { "Error querying PackageManager for launcher activities" }
				}
			}
		}

		logD(PM_COMPAT_TAG) { "Found ${loaded.size} launcher activities across ${profiles.size} profiles" }

		return loaded.sortedWith(
			compareBy<LauncherActivity> { it.packageName.lowercase() }
				.thenBy { it.label.lowercase() }
		)
	}

	override fun getAppIcon(packageName: String, userId: Int, isPrivate: Boolean): Drawable {
		val launcherApps = ctx.getSystemService(LauncherApps::class.java)
		val userManager = ctx.getSystemService(UserManager::class.java)

		val userHandle =
			userManager.userProfiles
				.firstOrNull { it.hashCode() == userId }
				?: myUserHandle()

		return try {
			val isMainProfile = userHandle == myUserHandle()

			if (!isMainProfile && !isPrivate && launcherApps != null) {
				val activities = launcherApps.getActivityList(packageName, userHandle)
				if (!activities.isNullOrEmpty()) {
					return activities[0].getBadgedIcon(0)
				}
				val appInfo =
					launcherApps.getApplicationInfo(packageName, 0, userHandle)
				return appInfo.loadIcon(pm)
			}

			if (isPrivate && launcherApps != null) {
				val activities = launcherApps.getActivityList(packageName, userHandle)
				if (!activities.isNullOrEmpty()) {
					return activities[0].getBadgedIcon(0)
				}
				val appInfo =
					launcherApps.getApplicationInfo(packageName, 0, userHandle)
				return appInfo.loadIcon(pm)
			}

			val appInfo = pm.getApplicationInfo(packageName, 0)
			appInfo.loadIcon(pm)
		} catch (e: Exception) {
			logE(ICONS_TAG, e) { "Error getting the app icon for $packageName, userId=$userId" }
			ContextCompat.getDrawable(ctx, R.drawable.ic_app_default)!!
		}
	}

	override fun getResourcesForApplication(packageName: String): Resources = pm.getResourcesForApplication(packageName)

	@RequiresApi(Build.VERSION_CODES.R)
	override fun queryAppShortcuts(packageName: String): List<ShortcutInfo> {
		try {
			val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return emptyList()

			val query =
				LauncherApps
					.ShortcutQuery()
					.setPackage(packageName)
					.setQueryFlags(
						LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
							LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
							LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
							LauncherApps.ShortcutQuery.FLAG_MATCH_CACHED
					)

			val userHandle = myUserHandle()
			val shortcuts = launcherApps.getShortcuts(query, userHandle)

			return shortcuts ?: emptyList()
		} catch (e: Exception) {
			logD(APPS_TAG) { e.toString() }
			return emptyList()
		}
	}

	override fun launchShortcut(packageName: String, id: String) {
		val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return
		try {
			launcherApps.startShortcut(packageName, id, null, null, myUserHandle())
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

//    override fun launchShortcut(launcherShortcut: ShortcutInfo, options: Bundle?): Boolean {
//        val launcherApps = ctx.getSystemService<LauncherApps>()!!
//        try {
//            launcherApps.startShortcut(launcherShortcut, null, options)
//        } catch (e: IllegalStateException) {
//            return false
//        } catch (e: ActivityNotFoundException) {
//            return false
//        } catch (e: SecurityException) {
//            return false
//        }
//        return true
//    }

	@RequiresApi(Build.VERSION_CODES.R)
	override fun loadShortcutIcon(
		packageName: String,
		shortcutId: String,
		sizePx: Int
	): Bitmap? {
		require(sizePx >= 1) {
			"Size must be >= 1"
		}
		try {
			val shortcuts = queryAppShortcuts(packageName)
			val shortcut = shortcuts.firstOrNull { it.id == shortcutId } ?: return null

			val densityDpi = ctx.resources.displayMetrics.densityDpi
			val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return null
			val drawable = launcherApps.getShortcutIconDrawable(shortcut, densityDpi) ?: return null

			return loadDrawableAsBitmap(drawable, sizePx, sizePx)
		} catch (e: Exception) {
			logE(ICONS_TAG, e) { "Error getting the shortcut icon for $packageName" }
			e.printStackTrace()
		}
		return null
	}
}
