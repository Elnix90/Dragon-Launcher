package org.elnix.dragonlauncher.base.model.serializables

import android.content.pm.ShortcutInfo
import android.os.Process
import android.os.UserHandle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.BluetoothADBCommands
import org.elnix.dragonlauncher.base.model.models.DataADBCommands
import org.elnix.dragonlauncher.base.model.models.DummyApp
import org.elnix.dragonlauncher.base.model.models.WifiADBCommands
import org.elnix.dragonlauncher.base.model.serializables.serializers.UserHandleSerializer
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.i18n.R

/**
 * Swipe Actions Serializable, the core of the main gesture idea
 * Holds all the different actions the user can do
 *
 * Here are all the related settings in which you have to add logic in order to correctly add a new action:
 *  - `actionLabel`
 *  - `launchAction`
 *  - `actionColor`
 */
@Immutable
@Serializable
@SerialName("Action")
sealed class Action {
	@get:DrawableRes
	abstract val drawableId: Int

	@get:StringRes
	abstract val resId: Int

	@Immutable
	@Serializable
	@SerialName("LaunchApp")
	data class LaunchApp(
		val packageName: String,
		val profile: Profile,
		@Transient
		val timerDuration: Int? = null
	) : Action() {
		@Transient
		override val drawableId: Int = R.drawable.apps

		@Transient
		override val resId: Int = R.string.open_app

		@Transient
		var appLabel: MutableState<String?> = mutableStateOf(null)

		constructor(application: Application) : this(
			application.packageName,
			application.profile
		)

		companion object {
			val dummy: LaunchApp = LaunchApp(DummyApp())
		}
	}

	@Immutable
	@Serializable
	@SerialName("LaunchShortcut")
	data class LaunchShortcut(
		val packageName: String,
		val shortcutId: String,
		@Serializable(UserHandleSerializer::class)
		val user: UserHandle
	) : Action() {
		override val drawableId: Int = R.drawable.ic_action_pinned_shortcut
		override val resId: Int = R.string.pinned_shortcuts

		companion object {
			val dummy: LaunchShortcut = LaunchShortcut("", "", Process.myUserHandle())

			fun ShortcutInfo.toAction(): LaunchShortcut = LaunchShortcut(`package`, id, userHandle)
		}
	}

	/**
	 * Launches a specific launcher activity of an app, identified by its [android.content.ComponentName]
	 * (package + activity class) instead of only the package name.
	 *
	 * Apps such as the MIUI/HyperOS dialer expose more than one launcher activity
	 * (e.g. Contacts and Phone in `com.android.contacts`), which cannot be represented
	 * by a plain [LaunchApp] action as the app list is keyed by package name (see #468).
	 */
	@Immutable
	@Serializable
	@SerialName("LaunchActivity")
	data class LaunchActivity(
		val packageName: String,
		val activityName: String,
		val profile: Profile
	) : Action() {
		override val drawableId: Int = R.drawable.open_in_new
		override val resId: Int = R.string.launch_activity

		companion object {
			val dummy: LaunchActivity = LaunchActivity("", "", Profile.dummy())
		}
	}

	@Immutable
	@Serializable
	@SerialName("OpenUrl")
	data class OpenUrl(
		val url: String
	) : Action() {
		override val drawableId: Int = R.drawable.web
		override val resId: Int = R.string.open_url

		companion object {
			val dummy: OpenUrl = OpenUrl("")
		}
	}

	@Immutable
	@Serializable
	@SerialName("OpenFile")
	data class OpenFile(
		val uri: String,
		val mimeType: String? = null
	) : Action() {
		override val drawableId: Int = R.drawable.ic_action_open_file
		override val resId: Int = R.string.open_file

		companion object {
			val dummy: OpenFile = OpenFile("")
		}
	}

	@Immutable
	@Serializable
	@SerialName("OpenAppDrawer")
	data class OpenAppDrawer(
		val workspaceId: String? = null
	) : Action() {
		override val drawableId: Int = R.drawable.workspaces
		override val resId: Int = R.string.app_drawer

		companion object {
			val dummy: OpenAppDrawer = OpenAppDrawer("Pick")
		}
	}

	@Immutable
	@Serializable
	@SerialName("OpenDragonLauncherSettings")
	data class OpenDragonLauncherSettings(
		val route: NavigationRoute = NavigationRoute.PointsSettings
	) : Action() {
		override val drawableId: Int = R.drawable.dragon_launcher_foreground
		override val resId: Int = R.string.dragon_launcher_settings

		companion object {
			val dummy: OpenDragonLauncherSettings = OpenDragonLauncherSettings()
		}
	}

	@Immutable
	@Serializable
	@SerialName("OpenCircleNest")
	data class OpenNest(
		val nestId: Int
	) : Action() {
		override val drawableId: Int = R.drawable.nest_icon
		override val resId: Int = R.string.open_nest

		companion object {
			val dummy: OpenNest = OpenNest(0)
		}
	}

	@Immutable
	@Serializable
	@SerialName("OpenWidget")
	data class OpenWidget(
		val widgetId: Int,
		val providerPackage: String,
		val providerClass: String
	) : Action() {
		override val drawableId: Int = R.drawable.widgets
		override val resId: Int = R.string.widgets

		companion object {
			val dummy: OpenWidget = OpenWidget(0, "", "")
		}
	}

	@Immutable
	@Serializable
	@SerialName("ToggleWifi")
	data class ToggleWifi(
		val command: WifiADBCommands = WifiADBCommands.Svc,
		val toast: Boolean? = false
	) : Action() {
		override val drawableId: Int = R.drawable.wifi
		override val resId: Int = R.string.toggle_wifi

		companion object {
			val dummy: ToggleWifi = ToggleWifi()
		}
	}

	@Immutable
	@Serializable
	@SerialName("ToggleBluetooth")
	data class ToggleBluetooth(
		val command: BluetoothADBCommands = BluetoothADBCommands.Cmd,
		val toast: Boolean? = false
	) : Action() {
		override val drawableId: Int = R.drawable.bluetooth
		override val resId: Int = R.string.toggle_bluetooth

		companion object {
			val dummy: ToggleBluetooth = ToggleBluetooth()
		}
	}

	@Immutable
	@Serializable
	@SerialName("ToggleData")
	data class ToggleData(
		val command: DataADBCommands = DataADBCommands.Svc,
		val toast: Boolean? = false
	) : Action() {
		override val drawableId: Int = R.drawable.cellular_icon
		override val resId: Int = R.string.toggle_mobile_data

		companion object {
			val dummy: ToggleData = ToggleData()
		}
	}

	@Immutable
	@Serializable
	@SerialName("RunAdbCommand")
	data class RunAdbCommand(
		val command: String,
		val toast: Boolean? = false
	) : Action() {
		override val drawableId: Int = R.drawable.adb_icon
		override val resId: Int = R.string.adb_command

		companion object {
			val dummy: RunAdbCommand = RunAdbCommand("")
		}
	}

	@Immutable
	@Serializable
	@SerialName("Lock")
	object Lock : Action() {
		override val drawableId: Int = R.drawable.lock
		override val resId: Int = R.string.lock
	}

	@Immutable
	@Serializable
	@SerialName("ReloadApps")
	object ReloadApps : Action() {
		override val drawableId: Int = R.drawable.reload
		override val resId: Int = R.string.reload_apps
	}

	@Immutable
	@Serializable
	@SerialName("OpenRecentApps")
	object OpenRecentApps : Action() {
		override val drawableId: Int = R.drawable.recent
		override val resId: Int = R.string.recent_apps
	}

	@Immutable
	@Serializable
	@SerialName("NotificationShade")
	object NotificationShade : Action() {
		override val drawableId: Int = R.drawable.notification
		override val resId: Int = R.string.notifications
	}

	@Immutable
	@Serializable
	@SerialName("ControlPanel")
	object ControlPanel : Action() {
		override val drawableId: Int = R.drawable.ic_action_grid
		override val resId: Int = R.string.control_panel
	}

	@Immutable
	@Serializable
	@SerialName("GoParentNest")
	object GoParentNest : Action() {
		override val drawableId: Int = R.drawable.fullscreen_exit
		override val resId: Int = R.string.go_parent_nest
	}

	@Immutable
	@Serializable
	@SerialName("KillLauncher")
	data object KillLauncher : Action() {
		override val drawableId: Int = R.drawable.skull
		override val resId: Int = R.string.kill_launcher
	}

	@Immutable
	@Serializable
	@SerialName("None")
	object None : Action() {
		override val drawableId: Int = R.drawable.remove
		override val resId: Int = R.string.none
	}

	companion object {
		fun Action?.actionColor(
			extraColors: ExtraColors,
			customColor: Color? = null
		): Color =
			customColor
				?: when (this) {
					is LaunchApp, is LaunchShortcut, is OpenWidget -> extraColors.launchApp
					is LaunchActivity -> extraColors.launchActivity
					is OpenUrl -> extraColors.openUrl
					is OpenAppDrawer -> extraColors.openAppDrawer
					is OpenDragonLauncherSettings -> extraColors.launcherSettings
					is OpenFile -> extraColors.openFile
					is ReloadApps -> extraColors.reload
					is OpenNest -> extraColors.openCircleNest
					is RunAdbCommand -> extraColors.runAdbCommand
					is ToggleBluetooth -> extraColors.toggleBluetooth
					is ToggleData -> extraColors.toggleData
					is ToggleWifi -> extraColors.toggleWifi
					NotificationShade -> extraColors.notificationShade
					ControlPanel -> extraColors.controlPanel
					Lock -> extraColors.lock
					OpenRecentApps -> extraColors.openRecentApps
					GoParentNest -> extraColors.goParentNest
					KillLauncher -> Color.Red
					None, null -> Color.Unspecified
				}

		val allActions: List<Action> =
			listOf(
				OpenWidget.dummy,
				LaunchApp.dummy,
				LaunchActivity.dummy,
				OpenNest.dummy,
				GoParentNest,
				LaunchShortcut.dummy,
				OpenUrl.dummy,
				OpenFile.dummy,
				NotificationShade,
				ControlPanel,
				OpenAppDrawer.dummy,
				Lock,
				ReloadApps,
				OpenRecentApps,
				OpenDragonLauncherSettings.dummy,
				RunAdbCommand.dummy,
				ToggleBluetooth.dummy,
				ToggleWifi.dummy,
				ToggleData.dummy,
				KillLauncher
			)

		object ActionJson : DragonJson<Action>()

		val actionsNumber: Int = Action::class.sealedSubclasses.size
	}
}
