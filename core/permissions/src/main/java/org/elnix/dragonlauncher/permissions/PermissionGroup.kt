package org.elnix.dragonlauncher.permissions

import androidx.annotation.StringRes
import org.elnix.dragonlauncher.i18n.R

/**
 * A permission the launcher needs, with the strings describing it to the user.
 */
public enum class PermissionGroup(
	@StringRes public val title: Int,
	@StringRes public val description: Int
) {
	Notifications(
		R.string.permission_notifications_title,
		R.string.permission_notifications_desc
	),
	Accessibility(
		R.string.permission_accessibility_title,
		R.string.permission_accessibility_desc
	),

	AppShortcuts(
		R.string.permission_app_shortcuts_title,
		R.string.permission_app_shortcuts_desc
	),

	DefaultLauncher(
		R.string.permission_manage_profiles_title,
		R.string.permission_manage_profiles_desc
	),

	UsageStat(
		R.string.permission_usage_stat_title,
		R.string.permission_usage_stat_desc
	)
}
