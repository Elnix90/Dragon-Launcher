package org.elnix.dragonlauncher.base.navigaton

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


@Suppress("EqualsOrHashCode")
@Serializable
public sealed class NavigationRoute : NavKey {

    @Serializable
    public data object Main : NavigationRoute()

    @Serializable
    public data object Drawer : NavigationRoute()

    @Serializable
    public data object Welcome : NavigationRoute()

    @Serializable
    public data class PointsSettings(
        val initialNestId: Int = 0
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
    }

    @Serializable
    public data object Settings : NavigationRoute()

    @Serializable
    public data object AppDisplay : NavigationRoute()

    @Serializable
    public data object Appearance : NavigationRoute()

    @Serializable
    public data object Colors : NavigationRoute()

    @Serializable
    public data object Wallpaper : NavigationRoute()

    @Serializable
    public data class Widgets(
        val nestId: Int = 0
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
    }

    @Serializable
    public data object IconPack : NavigationRoute()

    @Serializable
    public data object StatusBar : NavigationRoute()

    @Serializable
    public data object Fonts : NavigationRoute()

    @Serializable
    public data object Theme : NavigationRoute()

    @Serializable
    public data object AngleLineEdit : NavigationRoute()

    @Serializable
    public data object HoldToActivateArc : NavigationRoute()

    @Serializable
    public data object MainScreenLayers : NavigationRoute()

    @Serializable
    public data object Behavior : NavigationRoute()

    @Serializable
    public data object DrawerSettings : NavigationRoute()

    @Serializable
    public data object Workspace : NavigationRoute()

    @Serializable
    public data object Permissions : NavigationRoute()

    @Serializable
    public data object Backup : NavigationRoute()

    @Serializable
    public data object Wellbeing : NavigationRoute()

    @Serializable
    public data object Changelogs : NavigationRoute()

    @Serializable
    public data object Extensions : NavigationRoute()

    @Serializable
    public data object Debug : NavigationRoute()

    @Serializable
    public data object Logs : NavigationRoute()

    @Serializable
    public data class LogsViewer(
        val filename: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
    }

    @Serializable
    public data object SettingsJson : NavigationRoute()

    @Serializable
    public data class NestEdit(
        val nestId: Int = 0
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
    }

    @Serializable
    public data class WorkspaceDetail(
        val workspaceId: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
    }

    @Serializable
    public data class TimerExceeded(
        val appName: String
    ) : NavigationRoute() {
        override fun hashCode(): Int = super.hashCode()
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
                NestEdit(),
                WorkspaceDetail("")
            )
        }

    }

    public fun routeResId(route: NavigationRoute): Int {

        return when (route) {
            is Main -> R.string.main_screen
            is Drawer -> R.string.app_drawer
            is Welcome -> R.string.welcome_screen

            is PointsSettings -> R.string.points_settings
            is Settings -> R.string.settings
            is Appearance -> R.string.appearance
            is Colors -> R.string.color_selector
            is Wallpaper -> R.string.wallpaper
            is Widgets -> R.string.widgets
            is IconPack -> R.string.icon_pack
            is StatusBar -> R.string.status_bar
            is Fonts -> R.string.font_selector
            is Theme -> R.string.theme_selector
            is AngleLineEdit -> R.string.angle_line
            is HoldToActivateArc -> R.string.hold_settings
            is MainScreenLayers -> R.string.main_screen_layers
            is AppDisplay -> R.string.app_display

            is Behavior -> R.string.behavior
            is DrawerSettings -> R.string.app_drawer
            is Workspace -> R.string.workspaces
            is Permissions -> R.string.permissions
            is Backup -> R.string.backup_restore
            is Wellbeing -> R.string.wellbeing
            is Changelogs -> R.string.changelogs
            is Extensions -> R.string.extensions

            is Debug -> R.string.debug
            is Logs -> R.string.logs
            is LogsViewer -> R.string.logs
            is SettingsJson -> R.string.settings_json
            is NestEdit -> R.string.edit_nest
            is WorkspaceDetail -> R.string.workspaces
            is TimerExceeded -> R.string.time_exceeded_title
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