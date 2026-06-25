package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.dp
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.objects.enumList
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.objects.stringList
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.specialObjects.shape

@SettingsStore
object DrawerSettingsStore : MapSettingsStore() {

    @SettingKey
    val autoOpenSingleMatch = boolean(
        title = R.string.auto_launch_single_match,
        description = R.string.auto_launch_single_match_desc,
        default = true
    )

    @SettingKey
    val disableAutoLaunchOnSpaceFirstChar = boolean(
        title = R.string.disable_auto_launch_on_space_first_char,
        description = R.string.disable_auto_launch_on_space_first_char_desc,
        default = true
    )

    @SettingKey
    val showAppIconsInDrawer = boolean(
        title = R.string.show_app_icons_in_drawer,
        description = R.string.show_app_icons_in_drawer_desc,
        default = true
    )

    @SettingKey
    val showAppLabelInDrawer = boolean(
        title = R.string.show_app_labels_in_drawer,
        description = R.string.show_app_labels_in_drawer_desc,
        default = true
    )

    @SettingKey
    val autoShowKeyboardOnDrawer = boolean(
        title = R.string.auto_show_keyboard,
        description = R.string.auto_show_keyboard_desc,
        default = true
    )

    @SettingKey
    val tapEmptySpaceAction = enum(
        title = R.string.tap_empty_space_action,
        default = DrawerActions.Close
    )

    @SettingKey
    val gridSize = int(
        title = R.string.grid_size,
        default = 6,
        allowedRange = 1..15
    )

    @SettingKey
    val horizontalAlignment = enum(HorizontalAlignment.Start)

    @SettingKey
    val lastWorkspaceUsed = string("")

    @SettingKey
    val leftDrawerAction = enum(
        title = R.string.left_drawer_action,
        default = DrawerActions.defaultLeftDrawerAction
    )

    @SettingKey
    val rightDrawerAction = enum(
        title = R.string.right_drawer_action,
        default = DrawerActions.defaultRightDrawerAction
    )

    @SettingKey
    val leftDrawerWidth = dp(
        title = R.string.left_drawer_width,
        default = 0.dp,
        allowedRange = 0.dp..300.dp
    )

    @SettingKey
    val rightDrawerWidth = dp(
        title = R.string.right_drawer_width,
        default = 0.dp,
        allowedRange = 0.dp..300.dp
    )

    @SettingKey
    val drawerEnterAction = enum(
        title = R.string.drawer_enter_key_action,
        default = DrawerActions.defaultEnterAction
    )

    @SettingKey
    val drawerHomeAction = enum(
        title = R.string.home_action,
        description = R.string.home_action_desc,
        default = DrawerActions.defaultHomeAction
    )

    @SettingKey
    val scrollDownDrawerAction = enum(
        title = R.string.scroll_down_action,
        default = DrawerActions.defaultScrollDownAction
    )

    @SettingKey
    val scrollUpDrawerAction = enum(
        title = R.string.scroll_up_action,
        default = DrawerActions.defaultScrollUpAction
    )

    @SettingKey
    val backDrawerAction = enum(
        title = R.string.back_action,
        default = DrawerActions.defaultBackAction
    )

    @SettingKey
    val iconShape = shape(
        title = R.string.edit_icons_shape,
        description = R.string.edit_icons_shape_desc,
        default = IconShape.PlatformDefault
    )

    @SettingKey
    val iconsSpacingHorizontal = dp(
        title = R.string.icons_spacing_horizontal,
        description = R.string.icons_spacing_horizontal_desc,
        default = 8.dp,
        allowedRange = 0.dp..50.dp
    )

    @SettingKey
    val iconsSpacingVertical = dp(
        title = R.string.icons_spacing_vertical,
        description = R.string.icons_spacing_vertical_desc,
        default = 8.dp,
        allowedRange = 0.dp..50.dp

    )

    @SettingKey
    val iconSize = dp(
        description = R.string.max_icon_size_desc,
        title = R.string.max_icon_size,
        default = 96.dp,
        allowedRange = 0.dp..200.dp
    )

    @SettingKey
    val useCategory = boolean(
        title = R.string.use_categories,
        description = R.string.use_categories_desc,
        default = false
    )

    @SettingKey
    val categoryGridCells = int(
        title = R.string.category_cells,
        description = R.string.category_cells,
        default = 3,
        allowedRange = 2..5
    )

    @SettingKey
    val showCategoryName = boolean(
        title = R.string.show_category_name,
        description = R.string.show_category_name_desc,
        default = true
    )

    @SettingKey
    val showSearchBar = boolean(
        title = R.string.search_bar,
        default = true
    )

    @SettingKey
    val showRecentlyUsedApps = boolean(
        default = false,
        title = R.string.show_recently_used_apps,
        description = R.string.show_recently_used_apps_desc
    )

    @SettingKey
    val recentlyUsedAppsCount = int(
        default = 5,
        title = R.string.recently_used_apps_count,
        description = R.string.recently_used_apps_count_desc,
        allowedRange = 1..20
    )

    @SettingKey
    val recentlyUsedPackages = stringList(
        default = emptyList(),
        onChanged = {},
        backupable = false
    )

    @SettingKey
    val pullDownAnimations = boolean(
        title = R.string.pull_down_animations,
        description = R.string.pull_down_animations_desc,
        default = true
    )

    @SettingKey
    val pullDownWallPaperDim = boolean(
        title = R.string.pull_down_wallpaper_dim,
        description = R.string.pull_down_wallpaper_dim_desc,
        default = true
    )

//    @SettingKey
//    val pullDownIconFade = boolean(true)

    @SettingKey
    val pullDownScaleIn = boolean(
        title = R.string.pull_down_scale_in,
        description = R.string.pull_down_scale_in_desc,
        default = true
    )

    /**
     * The order of the search bar / recently used in drawer
     */
    @SettingKey
    val toolbarsOrder = enumList(
        title = R.string.toolbars_order,
        default = DrawerToolbar.defaultDrawerToolbarOrder
    )
}