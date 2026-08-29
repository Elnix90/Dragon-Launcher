package org.elnix.dragonlauncher.base.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R

@Immutable
@Suppress("EqualsOrHashCode")
@Serializable
public sealed class NavigationRoute : NavKey {

    @get:StringRes
    public abstract val resId: Int

    @get:DrawableRes
    public abstract val icon: Int

    @SerialName("Main")
    @Serializable
    public data object Main : NavigationRoute() {
        override val resId: Int = R.string.main_screen
        override val icon: Int = R.drawable.home
    }

    @Serializable
    @SerialName("Drawer")
    public data object Drawer : NavigationRoute() {
        override val resId: Int = R.string.drawer_screen
        override val icon: Int = R.drawable.workspaces
    }

    @Serializable
    @SerialName("Welcome")
    public data object Welcome : NavigationRoute() {
        override val resId: Int = R.string.welcome_screen
        override val icon: Int = R.drawable.rocket_launch
    }

    @Serializable
    @SerialName("PointsSettings")
    public data object PointsSettings : NavigationRoute() {
        override val resId: Int = R.string.points_settings
        override val icon: Int = R.drawable.settings
    }

    @Serializable
    @SerialName("Settings")
    public data object Settings : NavigationRoute() {
        override val resId: Int = R.string.settings
        override val icon: Int = R.drawable.settings
    }

    @Serializable
    @SerialName("AppDisplay")
    public data object AppDisplay : NavigationRoute() {
        override val resId: Int = R.string.app_display
        override val icon: Int = R.drawable.display_settings
    }

    @Serializable
    @SerialName("Appearance")
    public data object Appearance : NavigationRoute() {
        override val resId: Int = R.string.appearance
        override val icon: Int = R.drawable.routine
    }

    @Serializable
    @SerialName("Colors")
    public data object Colors : NavigationRoute() {
        override val resId: Int = R.string.color_selector
        override val icon: Int = R.drawable.palette
    }

    @Serializable
    @SerialName("Wallpaper")
    public data object Wallpaper : NavigationRoute() {
        override val resId: Int = R.string.wallpaper
        override val icon: Int = R.drawable.wallpaper
    }

    @Serializable
    @SerialName("Widgets")
    public data object Widgets: NavigationRoute() {
        override val resId: Int = R.string.widgets
        override val icon: Int = R.drawable.widgets
    }

    @Serializable
    @SerialName("Icons")
    public data object Icons : NavigationRoute() {
        override val resId: Int = R.string.icons_settings
        override val icon: Int = R.drawable.app_registration
    }

    @Serializable
    @SerialName("StatusBar")
    public data object StatusBar : NavigationRoute() {
        override val resId: Int = R.string.status_bar
        override val icon: Int = R.drawable.android_cell_5
    }

    @Serializable
    @SerialName("Fonts")
    public data object Fonts : NavigationRoute() {
        override val resId: Int = R.string.font_selector
        override val icon: Int = R.drawable.text_fields_alt
    }

    @Serializable
    @SerialName("Theme")
    public data object Theme : NavigationRoute() {
        override val resId: Int = R.string.theme_selector
        override val icon: Int = R.drawable.style
    }

    @Serializable
    @SerialName("AngleLineEdit")
    public data object AngleLineEdit : NavigationRoute() {
        override val resId: Int = R.string.angle_line
        override val icon: Int = R.drawable.polyline
    }

    @Serializable
    @SerialName("HoldToActivateArc")
    public data object HoldToActivateArc : NavigationRoute() {
        override val resId: Int = R.string.hold_settings
        override val icon: Int = R.drawable.shape_line
    }

    @Serializable
    @SerialName("MainScreenLayers")
    public data object MainScreenLayers : NavigationRoute() {
        override val resId: Int = R.string.main_screen_layers
        override val icon: Int = R.drawable.layers
    }

    @Serializable
    @SerialName("Behavior")
    public data object Behavior : NavigationRoute() {
        override val resId: Int = R.string.behavior
        override val icon: Int = R.drawable.question_mark
    }

    @Serializable
    @SerialName("DrawerSettings")
    public data object DrawerSettings : NavigationRoute() {
        override val resId: Int = R.string.drawer_settings
        override val icon: Int = R.drawable.workspaces
    }

    @Serializable
    @SerialName("Workspace")
    public data object Workspace : NavigationRoute() {
        override val resId: Int = R.string.workspaces
        override val icon: Int = R.drawable.workspaces
    }

    @Serializable
    @SerialName("Backup")
    public data object Backup : NavigationRoute() {
        override val resId: Int = R.string.backup
        override val icon: Int = R.drawable.reset
    }

    @Serializable
    @SerialName("Wellbeing")
    public data object Wellbeing : NavigationRoute() {
        override val resId: Int = R.string.wellbeing
        override val icon: Int = R.drawable.self_improvement
    }

    @Serializable
    @SerialName("Changelogs")
    public data object Changelogs : NavigationRoute() {
        override val resId: Int = R.string.changelogs
        override val icon: Int = R.drawable.source_notes
    }

    @Serializable
    @SerialName("Extensions")
    public data object Extensions : NavigationRoute() {
        override val resId: Int = R.string.extensions
        override val icon: Int = R.drawable.extension
    }

    @Serializable
    @SerialName("Debug")
    public data object Debug : NavigationRoute() {
        override val resId: Int = R.string.debug
        override val icon: Int = R.drawable.bug_report
    }

    @Serializable
    @SerialName("Logs")
    public data object Logs : NavigationRoute() {
        override val resId: Int = R.string.logs
        override val icon: Int = R.drawable.source_notes
    }

    @Serializable
    @SerialName("LogsViewer")
    public data class LogsViewer(
        val filename: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = R.string.logs
        override val icon: Int = R.drawable.source_notes
    }

    @Serializable
    @SerialName("SettingsJson")
    public data object SettingsJson : NavigationRoute() {
        override val resId: Int = R.string.settings_json
        override val icon: Int = R.drawable.settings
    }

    @Serializable
    @SerialName("NestEdit")
    public data object NestEdit : NavigationRoute() {
        override val resId: Int = R.string.nest_edition
        override val icon: Int = R.drawable.nest_icon
    }

    @Serializable
    @SerialName("WorkspaceDetail")
    public data class WorkspaceDetail(
        val workspaceId: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = R.string.edit_workspace
        override val icon: Int = R.drawable.edit_rounded
    }

    @Serializable
    @SerialName("TimerExceeded")
    public data class TimerExceeded(
        val appName: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = R.string.time_exceeded_title
        override val icon: Int = R.drawable.timer
    }

    @Serializable
    @SerialName("LockScreen")
    public data class LockScreen(
        val screenToGo: NavigationRoute
    ) : NavigationRoute() {
        override val resId: Int = R.string.lock
        override val icon: Int = R.drawable.lock
    }

    @Serializable
    @SerialName("LockScreenSetup")
    public data class LockScreenSetup(val lockMethod: LockMethod) : NavigationRoute() {
        override val resId: Int = R.string.lock
        override val icon: Int = R.drawable.lock
    }


    override fun hashCode(): Int = System.identityHashCode(this)

    public companion object {
        public val settingsRoutes: List<NavigationRoute> by lazy {
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
                SettingsJson,
                NestEdit,
                WorkspaceDetail("")
            )
        }

    }
}

/** List of routes that the routes killer ignores when the user leave the app for too long, usually files pickers */
public val NavKey.isIgnoredReturnScreen: Boolean
    get() = when (this) {
        NavigationRoute.Welcome,
        NavigationRoute.Backup,
        NavigationRoute.Wallpaper,
        NavigationRoute.Widgets -> true

        else -> false
    }

/** Screen that are transparents for the main scaffold, in order to see the wallpaper behind */
public val NavKey.inTransparentScreen: Boolean
    get() = when (this) {
        NavigationRoute.Main,
        NavigationRoute.Drawer,
        NavigationRoute.DrawerSettings,
        NavigationRoute.Wallpaper,
        NavigationRoute.Widgets,
        is NavigationRoute.LockScreen,
        is NavigationRoute.LockScreenSetup, -> true

        else -> false
    }

/** Screen that are transparents for the main scaffold, in order to see the wallpaper behind */
public val NavKey.halfTransparentScreen: Boolean
    get() = when (this) {
        NavigationRoute.PointsSettings,
        NavigationRoute.NestEdit -> true

        else -> false
    }