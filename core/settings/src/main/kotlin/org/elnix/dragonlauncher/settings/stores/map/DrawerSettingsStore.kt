package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.DpSettingObject.Companion.dp
import org.elnix.dragonlauncher.settings.bases.objects.EnumListSettingObject.Companion.enumList
import org.elnix.dragonlauncher.settings.bases.objects.EnumSettingObject.Companion.enum
import org.elnix.dragonlauncher.settings.bases.objects.IconShapeSettingObject.Companion.shape
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringListSettingObject.Companion.stringList
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object DrawerSettingsStore : MapSettingsStore(DataStoreName.DRAWER) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(
            this.autoOpenSingleMatch,
            this.disableAutoLaunchOnSpaceFirstChar,
            this.showAppIconsInDrawer,
            this.showAppLabelInDrawer,
            this.autoShowKeyboardOnDrawer,
            this.tapEmptySpaceAction,
            this.gridSize,
            this.horizontalAlignment,
            this.lastWorkspaceUsed,
            this.leftDrawerAction,
            this.rightDrawerAction,
            this.leftDrawerWidth,
            this.rightDrawerWidth,
            this.drawerEnterAction,
            this.drawerHomeAction,
            this.scrollDownDrawerAction,
            this.scrollUpDrawerAction,
            this.iconShape,
            this.iconsSpacingVertical,
            this.iconsSpacingHorizontal,
            this.iconSize,
            this.useCategory,
            this.showSearchBar,
            this.showRecentlyUsedApps,
            this.recentlyUsedAppsCount,
//            this.recentlyUsedPackages, // Don't put it in the ALL value, this way it won't be backup, and the onSetting changed wont trigger on new app launch
            this.categoryGridWidth,
            this.categoryGridCells,
            this.showCategoryName,
            this.backDrawerAction,
            this.pullDownAnimations,
            this.pullDownWallPaperDim,
//            this.pullDownIconFade,
            this.pullDownScaleIn,
            this.toolbarsOrder
        )
    }

    val autoOpenSingleMatch by boolean(
        title = R.string.auto_launch_single_match,
        description = R.string.auto_launch_single_match_desc,
        default = true
    )
    val disableAutoLaunchOnSpaceFirstChar by boolean(
        title = R.string.disable_auto_launch_on_space_first_char,
        description = R.string.disable_auto_launch_on_space_first_char_desc,
        default = true
    )

    val showAppIconsInDrawer by boolean(
        title = R.string.show_app_icons_in_drawer,
        description = R.string.show_app_icons_in_drawer_desc,
        default = true
    )

    val showAppLabelInDrawer by boolean(
        title = R.string.show_app_labels_in_drawer,
        description = R.string.show_app_labels_in_drawer_desc,
        default = true
    )

    val autoShowKeyboardOnDrawer by boolean(
        title = R.string.auto_show_keyboard,
        description = R.string.auto_show_keyboard_desc,
        default = true
    )

    val tapEmptySpaceAction by enum(
        title = R.string.tap_empty_space_action,
        description = null,
        default = DrawerActions.Close,
        enumClass = DrawerActions::class.java
    )

    val gridSize by int(
        title = R.string.grid_size,
        description = null,
        default = 6,
        allowedRange = 1..15
    )

    val horizontalAlignment by enum(
        title = null,
        description = null,
        default = HorizontalAlignment.Start,
        enumClass = HorizontalAlignment::class.java
    )

    val lastWorkspaceUsed by string(
        title = null,
        description = null,
        default = "",
    )

    val leftDrawerAction by enum(
        title = R.string.left_drawer_action,
        description = null,
        default = DrawerActions.defaultLeftDrawerAction,
        enumClass = DrawerActions::class.java
    )

    val rightDrawerAction by enum(
        title = R.string.right_drawer_action,
        description = null,
        default = DrawerActions.defaultRightDrawerAction,
        enumClass = DrawerActions::class.java
    )

    val leftDrawerWidth by dp(
        title = R.string.left_drawer_width,
        description = null,
        default = 0.dp,
        allowedRange = 0.dp..300.dp
    )

    val rightDrawerWidth by dp(
        title = R.string.right_drawer_width,
        description = null,
        default = 0.dp,
        allowedRange = 0.dp..300.dp
    )

    val drawerEnterAction by enum(
        title = R.string.drawer_enter_key_action,
        description = null,
        default = DrawerActions.defaultEnterAction,
        enumClass = DrawerActions::class.java
    )

    val drawerHomeAction by enum(
        title = R.string.home_action,
        description = R.string.home_action_desc,
        default = DrawerActions.defaultHomeAction,
        enumClass = DrawerActions::class.java
    )

    val scrollDownDrawerAction by enum(
        title = R.string.scroll_down_action,
        description = null,
        default = DrawerActions.defaultScrollDownAction,
        enumClass = DrawerActions::class.java
    )

    val scrollUpDrawerAction by enum(
        title = R.string.scroll_up_action,
        description = null,
        default = DrawerActions.defaultScrollUpAction,
        enumClass = DrawerActions::class.java
    )

    val backDrawerAction by enum(
        title = R.string.back_action,
        description = null,
        default = DrawerActions.defaultBackAction,
        enumClass = DrawerActions::class.java
    )

    val iconShape by shape(
        title = R.string.edit_icons_shape,
        description = R.string.edit_icons_shape_desc,
        default = IconShape.PlatformDefault
    )

    val iconsSpacingHorizontal by dp(
        title = R.string.icons_spacing_horizontal,
        description = R.string.icons_spacing_horizontal_desc,
        default = 8.dp,
        allowedRange = 0.dp..50.dp
    )

    val iconsSpacingVertical by dp(
        title = R.string.icons_spacing_vertical,
        description = R.string.icons_spacing_vertical_desc,
        default = 8.dp,
        allowedRange = 0.dp..50.dp

    )
    val iconSize by dp(
        description = R.string.max_icon_size_desc,
        title = R.string.max_icon_size,
        default = 96.dp,
        allowedRange = 0.dp..200.dp
    )

    val useCategory by boolean(
        title = R.string.use_categories,
        description = R.string.use_categories_desc,
        default = false
    )

    val categoryGridWidth by int(
        title = R.string.category_grid_width,
        description = null,
        default = 3,
        allowedRange = 1..4
    )

    val categoryGridCells by int(
        title = R.string.category_cells,
        description = R.string.category_cells,
        default = 3,
        allowedRange = 2..5
    )

    val showCategoryName by boolean(
        title = R.string.show_category_name,
        description = R.string.show_category_name_desc,
        default = true
    )

    val showSearchBar by boolean(
        title = R.string.search_bar,
        description = null,
        default = true
    )

    val showRecentlyUsedApps by boolean(
        default = false,
        title = R.string.show_recently_used_apps,
        description = R.string.show_recently_used_apps_desc
    )

    val recentlyUsedAppsCount by int(
        default = 5,
        title = R.string.recently_used_apps_count,
        description = R.string.recently_used_apps_count_desc,
        allowedRange = 1..20
    )

    val recentlyUsedPackages by stringList(
        title = null,
        description = null,
        default = emptyList()
    )

    val pullDownAnimations by boolean(
        title = R.string.pull_down_animations,
        description = R.string.pull_down_animations_desc,
        default = true
    )

    val pullDownWallPaperDim by boolean(
        title = R.string.pull_down_wallpaper_dim,
        description = R.string.pull_down_wallpaper_dim_desc,
        default = true
    )

//    val pullDownIconFade by boolean(
//        title = null,
//        description = null,
//        default = true
//    )

    val pullDownScaleIn by boolean(
        title = R.string.pull_down_scale_in,
        description = R.string.pull_down_scale_in_desc,
        default = true
    )

    /**
     * The order of the search bar / recently used in drawer
     */
    val toolbarsOrder by enumList(
        title = R.string.toolbars_order,
        description = null,
        default = DrawerToolbar.defaultDrawerToolbarOrder,
        enumClass = DrawerToolbar::class.java
    )
}