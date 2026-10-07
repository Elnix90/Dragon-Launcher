package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object PrivateSettingsStore : MapSettingsStore(backupable = false) {
	@SettingKey
	val hasSeenWelcomeScreen = boolean(
		title = R.string.has_seen_welcome,
		description = R.string.has_seen_welcome_desc,
		icon = R.drawable.rocket_launch,
		default = false
	)

	/**
	 * That's the original setting key, only old users will have this one set to true
	 * and therefore, they'll see the message in the welcome screen
	 */
	@SettingKey
	val hasSeenWelcome = boolean(
		title = R.string.has_seen_welcome_old,
		description = R.string.has_seen_welcome_old_desc,
		icon = R.drawable.timer,
		default = false
	)

	@SettingKey
	val hasInitialized =
		boolean(
			title = R.string.has_initialized,
			description = R.string.has_initialized_desc,
			default = false
		)

	@SettingKey
	val showSetDefaultLauncherBanner =
		boolean(
			title = R.string.show_set_default_launcher_banner,
			description = R.string.show_set_default_launcher_banner_desc,
			icon = R.drawable.question_mark,
			default = true
		)

	@SettingKey
	val showReselectBackupBanner =
		boolean(
			title = R.string.show_reselect_backup_banner,
			description = R.string.show_set_default_launcher_banner_desc,
			icon = R.drawable.question_mark,
			default = true
		)

	@SettingKey
	val hideBetaVersionWarning =
		boolean(
			title = R.string.hide_beta_version_warning,
			description = R.string.hide_beta_version_warning_desc,
			icon = R.drawable.warning,
			default = false
		)

	@SettingKey
	val lastSeenVersionCodeWhatsNew =
		int(
			default = 0,
			allowedRange = 0..Int.MAX_VALUE
		)

	@SettingKey
	val lastSeenVersionCodeGoogleLockdownWarning =
		int(
			default = Int.MAX_VALUE,
			allowedRange = 0..Int.MAX_VALUE
		)

	@SettingKey
	val installVersionCode =
		int(
			default = -1,
			allowedRange = -1..Int.MAX_VALUE
		)

	/**
	 *  Hashed code for settings lock (SHA-256).
	 *  This can contain either the Pattern hashed or the PIN hashed.
	 *  They are both handled as string, containing the digits in the LtR direction:
	 */
	@SettingKey
	val settingsHash = string("")

// 	/**
// 	 *  Hashed code for launching actions (SHA-256).
// 	 *  This can contain either the Pattern hashed or the PIN hashed.
// 	 *  They are both handled as string, containing the digits in the LtR direction:
// 	 */
// 	@SettingKey
// 	val actionsHash = string("")

	@SettingKey
	val lockMethod = enum(LockMethod.None)

// 	/**
// 	 * The lock method for the actions
// 	 */
// 	@SettingKey
// 	val actionsLockMethod = enum(LockMethod.None)

//    /**
//     * Used to remember the page the user left when exiting the welcome screen, and going, for example to the default launcher selection
//     */
//    @SettingKey
//    public val welcomeScreenTempPage =
//        int(
//            default = 0,
//            allowedRange = 0..6
//        )

	@SettingKey
	val lastCrashStackTrace = string("")
}
