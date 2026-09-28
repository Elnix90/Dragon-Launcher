package org.elnix.dragonlauncher.permissions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.elnix.dragonlauncher.i18n.R

/**
 * A permission the launcher needs, with the strings describing it to the user.
 */
public enum class PermissionGroup(
	@StringRes public val title: Int,
	@StringRes public val description: Int,
	@DrawableRes public val icon: Int
) {
	Notifications(
		R.string.permission_notifications_title,
		R.string.permission_notifications_desc,
		R.drawable.notification
	),
	Accessibility(
		R.string.permission_accessibility_title,
		R.string.permission_accessibility_desc,
		R.drawable.accessibility_new
	),

	AppShortcuts(
		R.string.permission_app_shortcuts_title,
		R.string.permission_app_shortcuts_desc,
		R.drawable.ic_action_pinned_shortcut
	),

	DefaultLauncher(
		R.string.permission_manage_profiles_title,
		R.string.permission_manage_profiles_desc,
		R.drawable.rocket_launch
	),

	UsageStat(
		R.string.permission_usage_stat_title,
		R.string.permission_usage_stat_desc,
		R.drawable.analytics
	),

	DisplayOverOtherApps(
		R.string.permission_display_over_other_apps_title,
		R.string.permission_display_over_other_apps_desc,
		R.drawable.visibility
	)
}
