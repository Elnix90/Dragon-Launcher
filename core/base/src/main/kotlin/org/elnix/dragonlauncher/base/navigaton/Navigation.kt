package org.elnix.dragonlauncher.base.navigaton

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Backup
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Drawer
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.DrawerSettings
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Main
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Wallpaper
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Welcome
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Widgets
import org.elnix.dragonlauncher.i18n.R


@Serializable
sealed class NavigationRoute : NavKey {

    @Serializable
    data object Main : NavigationRoute()

    @Serializable
    data object Drawer : NavigationRoute()

    @Serializable
    data object Welcome : NavigationRoute()

    @Serializable
    data class PointsSettings(
        val initialNestId: Int = 0
    ) : NavigationRoute()

    @Serializable
    data object Settings : NavigationRoute()

    @Serializable
    data object AppDisplay : NavigationRoute()

    @Serializable
    data object Appearance : NavigationRoute()

    @Serializable
    data object Colors : NavigationRoute()

    @Serializable
    data object Wallpaper : NavigationRoute()

    @Serializable
    data class Widgets(
        val nestId: Int = 0
    ) : NavigationRoute()

    @Serializable
    data object IconPack : NavigationRoute()

    @Serializable
    data object StatusBar : NavigationRoute()

    @Serializable
    data object Fonts : NavigationRoute()

    @Serializable
    data object Theme : NavigationRoute()

    @Serializable
    data object AngleLineEdit : NavigationRoute()

    @Serializable
    data object HoldToActivateArc : NavigationRoute()

    @Serializable
    data object MainScreenLayers : NavigationRoute()

    @Serializable
    data object Behavior : NavigationRoute()

    @Serializable
    data object DrawerSettings : NavigationRoute()

    @Serializable
    data object Workspace : NavigationRoute()

    @Serializable
    data object Permissions : NavigationRoute()

    @Serializable
    data object Backup : NavigationRoute()

    @Serializable
    data object Wellbeing : NavigationRoute()

    @Serializable
    data object Changelogs : NavigationRoute()

    @Serializable
    data object Extensions : NavigationRoute()

    @Serializable
    data object Debug : NavigationRoute()

    @Serializable
    data object Logs : NavigationRoute()

    @Serializable
    data class LogsViewer(
        val filename: String
    ) : NavigationRoute()

    @Serializable
    data object SettingsJson : NavigationRoute()

    @Serializable
    data class NestEdit(
        val nestId: Int = 0
    ) : NavigationRoute()

    @Serializable
    data class WorkspaceDetail(
        val workspaceId: String
    ) : NavigationRoute()

    companion object {
        val settingsRoutes: List<NavigationRoute> by lazy {
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

    fun routeResId(route: NavigationRoute): Int {

        return when (route) {
            is Main -> R.string.points_settings
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
        }
    }
}

/** List of routes that the routes killer ignores when the user leave the app for too long, usually files pickers */
fun NavKey.isInIgnoredReturnScreen(): Boolean =
    when (this) {
        Welcome,
        Backup,
        Wallpaper,
        is Widgets -> true

        else -> false
    }

/** Screen that are transparents for the main scaffold, in order to see the wallpaper behind */
fun NavKey.isInTransparentScreen(): Boolean =
    when (this) {
        Main,
        Drawer,
        DrawerSettings,
        Wallpaper,
        is Widgets -> true

        else -> false
    }