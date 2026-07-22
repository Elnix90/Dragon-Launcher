package org.elnix.dragonlauncher.base.navigaton

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Backup
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Drawer
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.DrawerSettings
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Main
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Wallpaper
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Welcome
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Widgets
import org.elnix.dragonlauncher.i18n.R

private const val Undefined: Int = -1

@Suppress("EqualsOrHashCode")
@Serializable
public sealed class NavigationRoute : NavKey {

    @get:StringRes
    public abstract val resId: Int

    @get:DrawableRes
    public abstract val icon: Int

    @Serializable
    public data object Main : NavigationRoute() {
        override val resId: Int = R.string.main_screen
        override val icon: Int = Undefined
    }

    @Serializable
    public data object Drawer : NavigationRoute() {
        override val resId: Int = R.string.drawer_screen
        override val icon: Int = R.drawable.workspaces
    }

    @Serializable
    public data object Welcome : NavigationRoute() {
        override val resId: Int = R.string.welcome_screen
        override val icon: Int = R.drawable.rocket_launch
    }

    @Serializable
    public data class PointsSettings(
        val initialNestId: Int = 0
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = R.string.points_settings
        override val icon: Int = R.drawable.settings
    }

    @Serializable
    public data object Settings : NavigationRoute() {
        override val resId: Int = R.string.settings
        override val icon: Int = R.drawable.settings
    }

    @Serializable
    public data object AppDisplay : NavigationRoute() {
        override val resId: Int = R.string.app_display
        override val icon: Int = R.drawable.display_settings
    }

    @Serializable
    public data object Appearance : NavigationRoute() {
        override val resId: Int = R.string.appearance
        override val icon: Int = R.drawable.routine
    }

    @Serializable
    public data object Colors : NavigationRoute() {
        override val resId: Int = R.string.color_selector
        override val icon: Int = R.drawable.palette
    }

    @Serializable
    public data object Wallpaper : NavigationRoute() {
        override val resId: Int = R.string.wallpaper
        override val icon: Int = R.drawable.wallpaper
    }

    @Serializable
    public data class Widgets(
        val nestId: Int = 0
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = R.string.widgets
        override val icon: Int = R.drawable.widgets
    }

    @Serializable
    public data object IconPack : NavigationRoute() {
        override val resId: Int = R.string.icon_packs
        override val icon: Int = R.drawable.app_registration
    }

    @Serializable
    public data object StatusBar : NavigationRoute() {
        override val resId: Int = R.string.status_bar
        override val icon: Int = R.drawable.android_cell_5
    }

    @Serializable
    public data object Fonts : NavigationRoute() {
        override val resId: Int = R.string.font_selector
        override val icon: Int = R.drawable.text_fields_alt
    }

    @Serializable
    public data object Theme : NavigationRoute() {
        override val resId: Int = R.string.theme_selector
        override val icon: Int = R.drawable.style
    }

    @Serializable
    public data object AngleLineEdit : NavigationRoute() {
        override val resId: Int = R.string.angle_line
        override val icon: Int = R.drawable.polyline
    }

    @Serializable
    public data object HoldToActivateArc : NavigationRoute() {
        override val resId: Int = R.string.hold_settings
        override val icon: Int = R.drawable.shape_line
    }

    @Serializable
    public data object MainScreenLayers : NavigationRoute() {
        override val resId: Int = R.string.main_screen_layers
        override val icon: Int = R.drawable.layers
    }

    @Serializable
    public data object Behavior : NavigationRoute() {
        override val resId: Int = R.string.behavior
        override val icon: Int = R.drawable.question_mark
    }

    @Serializable
    public data object DrawerSettings : NavigationRoute() {
        override val resId: Int = R.string.drawer_settings
        override val icon: Int = R.drawable.workspaces
    }

    @Serializable
    public data object Workspace : NavigationRoute() {
        override val resId: Int = R.string.workspaces
        override val icon: Int = R.drawable.workspaces
    }

    @Serializable
    public data object Permissions : NavigationRoute() {
        override val resId: Int = R.string.permissions
        override val icon: Int = R.drawable.privacy_tip
    }

    @Serializable
    public data object Backup : NavigationRoute() {
        override val resId: Int = R.string.backup
        override val icon: Int = R.drawable.reset
    }

    @Serializable
    public data object Wellbeing : NavigationRoute() {
        override val resId: Int = R.string.wellbeing
        override val icon: Int = R.drawable.self_improvement
    }

    @Serializable
    public data object Changelogs : NavigationRoute() {
        override val resId: Int = R.string.changelogs
        override val icon: Int = R.drawable.source_notes
    }

    @Serializable
    public data object Extensions : NavigationRoute() {
        override val resId: Int = R.string.extensions
        override val icon: Int = R.drawable.extension
    }

    @Serializable
    public data object Debug : NavigationRoute() {
        override val resId: Int = R.string.debug
        override val icon: Int = R.drawable.bug_report
    }

    @Serializable
    public data object Logs : NavigationRoute() {
        override val resId: Int = R.string.logs
        override val icon: Int = R.drawable.source_notes
    }

    @Serializable
    public data class LogsViewer(
        val filename: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = R.string.logs
        override val icon: Int = Undefined
    }

    @Serializable
    public data object SettingsJson : NavigationRoute() {
        override val resId: Int = R.string.settings_json
        override val icon: Int = R.drawable.settings
    }

    @Serializable
    public data object NestEdit : NavigationRoute() {
        override val resId: Int = R.string.nest_edition
        override val icon: Int = R.drawable.nest_icon
    }

    @Serializable
    public data class WorkspaceDetail(
        val workspaceId: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = Undefined
        override val icon: Int = Undefined
    }

    @Serializable
    public data class TimerExceeded(
        val appName: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
        override val resId: Int = R.string.time_exceeded_title
        override val icon: Int = R.drawable.timer
    }

    override fun hashCode(): Int = System.identityHashCode(this)

    public companion object {
        public val settingsRoutes: List<NavigationRoute> by lazy {
            listOf(
                PointsSettings(),
                Settings,
                Appearance,
                AppDisplay,
                Colors,
                Wallpaper,
                Widgets(),
                IconPack,
                StatusBar,
                Fonts,
                Theme,
                AngleLineEdit,
                HoldToActivateArc,
                MainScreenLayers,
                Behavior,
                DrawerSettings,
                Workspace,
                Permissions,
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
public val NavKey.isInIgnoredReturnScreen: Boolean
    get() = when (this) {
        Welcome,
        Backup,
        Wallpaper,
        is Widgets -> true

        else -> false
    }

/** Screen that are transparents for the main scaffold, in order to see the wallpaper behind */
public val NavKey.isInTransparentScreen: Boolean
    get() = when (this) {
        Main,
        Drawer,
        DrawerSettings,
        Wallpaper,
//        is PointsSettings,
//        is NestEdit,
        is Widgets -> true

        else -> false
    }