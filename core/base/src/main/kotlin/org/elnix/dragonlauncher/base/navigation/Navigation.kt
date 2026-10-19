package org.elnix.dragonlauncher.base.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.i18n.R

@Immutable
@Suppress("EqualsOrHashCode")
@Serializable
sealed class NavigationRoute : NavKey {
	@get:StringRes
	abstract val resId: Int

	@get:DrawableRes
	abstract val icon: Int

	@SerialName("Main")
	@Serializable
	data object Main : NavigationRoute() {
		override val resId: Int = R.string.main_screen
		override val icon: Int = R.drawable.home
	}

	@Serializable
	@SerialName("Drawer")
	data object Drawer : NavigationRoute() {
		override val resId: Int = R.string.drawer_screen
		override val icon: Int = R.drawable.workspaces
	}

	@Serializable
	@SerialName("Welcome")
	data object Welcome : NavigationRoute() {
		override val resId: Int = R.string.welcome_screen
		override val icon: Int = R.drawable.rocket_launch
	}

	@Serializable
	@SerialName("PointsSettings")
	data object PointsSettings : NavigationRoute() {
		override val resId: Int = R.string.points_settings
		override val icon: Int = R.drawable.settings
	}

	@Serializable
	@SerialName("Settings")
	data object Settings : NavigationRoute() {
		override val resId: Int = R.string.settings
		override val icon: Int = R.drawable.settings
	}

	@Serializable
	@SerialName("AppDisplay")
	data object AppDisplay : NavigationRoute() {
		override val resId: Int = R.string.app_display
		override val icon: Int = R.drawable.display_settings
	}

	@Serializable
	@SerialName("Appearance")
	data object Appearance : NavigationRoute() {
		override val resId: Int = R.string.appearance
		override val icon: Int = R.drawable.routine
	}

	@Serializable
	@SerialName("Colors")
	data object Colors : NavigationRoute() {
		override val resId: Int = R.string.color_selector
		override val icon: Int = R.drawable.palette
	}

	@Serializable
	@SerialName("Wallpaper")
	data object Wallpaper : NavigationRoute() {
		override val resId: Int = R.string.wallpaper
		override val icon: Int = R.drawable.wallpaper
	}

	@Serializable
	@SerialName("Widgets")
	data object Widgets : NavigationRoute() {
		override val resId: Int = R.string.widgets
		override val icon: Int = R.drawable.widgets
	}

	@Serializable
	@SerialName("Icons")
	data object Icons : NavigationRoute() {
		override val resId: Int = R.string.icons_settings
		override val icon: Int = R.drawable.app_registration
	}

	@Serializable
	@SerialName("StatusBar")
	data object StatusBar : NavigationRoute() {
		override val resId: Int = R.string.status_bar
		override val icon: Int = R.drawable.android_cell_5
	}

	@Serializable
	@SerialName("Fonts")
	data object Fonts : NavigationRoute() {
		override val resId: Int = R.string.font_selector
		override val icon: Int = R.drawable.text_fields_alt
	}

	@Serializable
	@SerialName("Theme")
	data object Theme : NavigationRoute() {
		override val resId: Int = R.string.theme_selector
		override val icon: Int = R.drawable.style
	}

	@Serializable
	@SerialName("AngleLineEdit")
	data object AngleLineEdit : NavigationRoute() {
		override val resId: Int = R.string.angle_line
		override val icon: Int = R.drawable.polyline
	}

	@Serializable
	@SerialName("HoldToActivateArc")
	data object HoldToActivateArc : NavigationRoute() {
		override val resId: Int = R.string.hold_settings
		override val icon: Int = R.drawable.shape_line
	}

	@Serializable
	@SerialName("MainScreenLayers")
	data object MainScreenLayers : NavigationRoute() {
		override val resId: Int = R.string.main_screen_layers
		override val icon: Int = R.drawable.layers
	}

	@Serializable
	@SerialName("Behavior")
	data object Behavior : NavigationRoute() {
		override val resId: Int = R.string.behavior
		override val icon: Int = R.drawable.question_mark
	}

	@Serializable
	@SerialName("GlobalDraggingModeSetup")
	data object GlobalDraggingModeSetup : NavigationRoute() {
		override val resId: Int = R.string.global_dragging_mode
		override val icon: Int = R.drawable.settings
	}

	@Serializable
	@SerialName("DrawerSettings")
	data object DrawerSettings : NavigationRoute() {
		override val resId: Int = R.string.drawer_settings
		override val icon: Int = R.drawable.workspaces
	}

	@Serializable
	@SerialName("Workspace")
	data object Workspace : NavigationRoute() {
		override val resId: Int = R.string.workspaces
		override val icon: Int = R.drawable.workspaces
	}

	@Serializable
	@SerialName("Backup")
	data object Backup : NavigationRoute() {
		override val resId: Int = R.string.backup
		override val icon: Int = R.drawable.reset
	}

	@Serializable
	@SerialName("Wellbeing")
	data object Wellbeing : NavigationRoute() {
		override val resId: Int = R.string.wellbeing
		override val icon: Int = R.drawable.self_improvement
	}

	@Serializable
	@SerialName("ChargingAnimation")
	data object ChargingAnimation : NavigationRoute() {
		override val resId: Int = R.string.charging_animation
		override val icon: Int = R.drawable.battery_charging
	}

	@Serializable
	@SerialName("DigitalPauseScreen")
	data class DigitalPauseScreen(
		val appToLaunch: Action.LaunchApp
	) : NavigationRoute() {
		override val resId: Int = R.string.wellbeing
		override val icon: Int = R.drawable.self_improvement
	}

	@Serializable
	@SerialName("Changelogs")
	data object Changelogs : NavigationRoute() {
		override val resId: Int = R.string.changelogs
		override val icon: Int = R.drawable.source_notes
	}

	@Serializable
	@SerialName("Extensions")
	data object Extensions : NavigationRoute() {
		override val resId: Int = R.string.extensions
		override val icon: Int = R.drawable.extension
	}

	@Serializable
	@SerialName("Debug")
	data object Debug : NavigationRoute() {
		override val resId: Int = R.string.debug
		override val icon: Int = R.drawable.bug_report
	}

	@Serializable
	@SerialName("Logs")
	data object Logs : NavigationRoute() {
		override val resId: Int = R.string.logs
		override val icon: Int = R.drawable.source_notes
	}

	@Serializable
	@SerialName("Permissions")
	data object Permissions : NavigationRoute() {
		override val resId: Int = R.string.permissions
		override val icon: Int = R.drawable.encrypted
	}

	@Serializable
	@SerialName("LogsViewer")
	data class LogsViewer(
		val filename: String
	) : NavigationRoute() {
		override val resId: Int = R.string.logs
		override val icon: Int = R.drawable.source_notes
	}

	@Serializable
	@SerialName("SettingsJson")
	data object SettingsJson : NavigationRoute() {
		override val resId: Int = R.string.settings_json
		override val icon: Int = R.drawable.settings
	}

	@Serializable
	@SerialName("NestEdit")
	data object NestEdit : NavigationRoute() {
		override val resId: Int = R.string.nest_edition
		override val icon: Int = R.drawable.nest_icon
	}

	@Serializable
	@SerialName("WorkspaceDetail")
	data class WorkspaceDetail(
		val workspaceId: String
	) : NavigationRoute() {
		override val resId: Int = R.string.edit_workspace
		override val icon: Int = R.drawable.edit_rounded
	}

	@Serializable
	@SerialName("TimerExceeded")
	data class TimerExceeded(
		val appName: String
	) : NavigationRoute() {
		override val resId: Int = R.string.time_exceeded_title
		override val icon: Int = R.drawable.timer
	}

	@Serializable
	@SerialName("LockScreen")
	data class LockScreen(
		val screenToGo: NavigationRoute
	) : NavigationRoute() {
		override val resId: Int = R.string.lock
		override val icon: Int = R.drawable.lock
	}

	@Serializable
	@SerialName("LockScreenSetup")
	data class LockScreenSetup(
		val lockMethod: LockMethod
	) : NavigationRoute() {
		override val resId: Int = R.string.lock
		override val icon: Int = R.drawable.lock
	}

	override fun hashCode(): Int = System.identityHashCode(this)

	companion object {
		val settingsRoutes: List<NavigationRoute> by lazy {
			listOf(
				PointsSettings,
				Settings,
				Appearance,
				AppDisplay,
				Colors,
				Wallpaper,
				Widgets,
				Icons,
				StatusBar,
				Fonts,
				Theme,
				AngleLineEdit,
				HoldToActivateArc,
				MainScreenLayers,
				Behavior,
				DrawerSettings,
				Workspace,
				Backup,
				Wellbeing,
				Changelogs,
				Extensions,
				Debug,
				Logs,
				Permissions,
				SettingsJson,
				NestEdit,
				WorkspaceDetail("")
			)
		}
	}
}

/** List of routes that the routes killer ignores when the user leave the app for too long, usually files pickers */
val NavKey.isIgnoredReturnScreen: Boolean
	get() =
		when (this) {
			NavigationRoute.Welcome,
			NavigationRoute.Backup,
			NavigationRoute.Wallpaper,
			NavigationRoute.Widgets
			-> true

			else -> false
		}

/** Screen that are transparents for the main scaffold, in order to see the wallpaper behind */
val NavKey.inTransparentScreen: Boolean
	get() =
		when (this) {
			NavigationRoute.Main,
			NavigationRoute.Drawer,
			NavigationRoute.Wallpaper,
			NavigationRoute.Widgets,
			NavigationRoute.Welcome,
			is NavigationRoute.LockScreen,
			is NavigationRoute.LockScreenSetup -> true

			else -> false
		}

/** Screen that are transparents for the main scaffold, in order to see the wallpaper behind */
val NavKey.halfTransparentScreen: Boolean
	get() =
		when (this) {
			NavigationRoute.PointsSettings,
			NavigationRoute.NestEdit -> true

			else -> false
		}
