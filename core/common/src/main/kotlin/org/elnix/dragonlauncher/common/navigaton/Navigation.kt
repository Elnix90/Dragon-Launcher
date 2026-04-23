package org.elnix.dragonlauncher.common.navigaton


// ──────────────────── SETTINGS ────────────────────

object SETTINGS {
    const val ROOT = "settings"
    const val ADVANCED_ROOT = "settings/advanced"
    const val APPEARANCE = "settings/advanced/appearance"
    const val WALLPAPER = "settings/advanced/appearance/wallpaper"
    const val ICON_PACK = "settings/advanced/appearance/icon_pack"
    const val STATUS_BAR = "settings/advanced/appearance/status_bar"
    const val THEME = "settings/advanced/appearance/theme"
    const val FONTS = "settings/advanced/appearance/fonts"
//    const val PERMISSIONS = "settings/advanced/permissions"
    const val BEHAVIOR = "settings/advanced/behavior"
    const val COLORS = "settings/advanced/appearance/colors"
    const val DRAWER = "settings/advanced/drawer"
    const val WORKSPACE = "settings/advanced/workspace"
    const val BACKUP = "settings/advanced/backup"
    const val WELLBEING = "settings/advanced/wellbeing"
    const val DEBUG = "settings/advanced/debug"
    const val LOGS = "settings/advanced/debug/logs"
    const val SETTINGS_JSON = "settings/advanced/debug/settings_json"
    const val LANGUAGE = "settings/advanced/language"
    const val CHANGELOGS = "settings/advanced/changelogs"
    const val EXTENSIONS = "settings/advanced/extensions"
    const val ANGLE_LINE_EDIT = "settings/advanced/angleLineEdit"
    const val HOLD_TO_ACTIVATE_ARC = "settings/advanced/hold_to_activate"
    const val MAINS_SCREEN_LAYERS = "settings/advanced/main_screen_layers"
    const val WIDGETS_FLOATING_APPS = "settings/advanced/widgets_floating_apps/{id}"
    const val LOGS_VIEWER_SCREEN = "settings/advanced/debug/logs/viewer/{filename}"

}

@Suppress("ClassName")
object EDIT_SCREENS {
    const val WORKSPACE_DETAIL = "settings/advanced/workspace/{id}"
    const val NESTS_EDIT = "settings/nest/{id}"
}

object ROUTES {
    const val MAIN = "main"
    const val DRAWER = "drawer"
    const val WELCOME = "welcome"
}

val homeRoutes = listOf(
    ROUTES.MAIN,
    ROUTES.DRAWER,
    ROUTES.WELCOME
)