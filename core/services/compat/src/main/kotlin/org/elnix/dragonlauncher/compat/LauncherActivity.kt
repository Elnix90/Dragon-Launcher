package org.elnix.dragonlauncher.compat

import android.content.ComponentName
import org.elnix.dragonlauncher.base.model.serializables.Profile

/**
 * A single launcher activity entry returned by [PackageManagerCompat.getLauncherActivities].
 */
data class LauncherActivity(
	val componentName: ComponentName,
	val label: String,
	val profile: Profile
) {
	val packageName: String get() = componentName.packageName
	val className: String get() = componentName.className

	fun matchesSearch(query: String, appName: String): Boolean =
		label.contains(query, ignoreCase = true) ||
			appName.contains(query, ignoreCase = true) ||
			packageName.contains(query, ignoreCase = true) ||
			className.contains(query, ignoreCase = true)
}
