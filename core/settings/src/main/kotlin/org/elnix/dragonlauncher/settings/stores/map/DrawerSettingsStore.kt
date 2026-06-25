package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.DpSettingObject
import io.github.elnix90.core.objects.EnumListSettingObject
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.StringListSettingObject
import io.github.elnix90.core.objects.StringSettingObject
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
import org.elnix.dragonlauncher.settings.specialObjects.IconShapeSettingObject
import org.elnix.dragonlauncher.settings.specialObjects.shape

@SettingsStore
public object DrawerSettingsStore : MapSettingsStore() {

    @SettingKey
    public val autoOpenSingleMatch: BooleanSettingObject = boolean(
        title = R.string.auto_launch_single_match,
        description = R.string.auto_launch_single_match_desc,
        default = true
    )

    @SettingKey
    public val disableAutoLaunchOnSpaceFirstChar: BooleanSettingObject = boolean(
        title = R.string.disable_auto_launch_on_space_first_char,
        description = R.string.disable_auto_launch_on_space_first_char_desc,
        default = true
    )

    @SettingKey
    public val showAppIconsInDrawer: BooleanSettingObject = boolean(
        title = R.string.show_app_icons_in_drawer,
        description = R.string.show_app_icons_in_drawer_desc,
        default = true
    )

    @SettingKey
    public val showAppLabelInDrawer: BooleanSettingObject = boolean(
        title = R.string.show_app_labels_in_drawer,
        description = R.string.show_app_labels_in_drawer_desc,
        default = true
    )

    @SettingKey
    public val autoShowKeyboardOnDrawer: BooleanSettingObject = boolean(
        title = R.string.auto_show_keyboard,
        description = R.string.auto_show_keyboard_desc,
        default = true
    )

    @SettingKey
    public val tapEmptySpaceAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.tap_empty_space_action,
        default = DrawerActions.Close
    )

    @SettingKey
    public val gridSize: IntSettingObject = int(
        title = R.string.grid_size,
        default = 6,
        allowedRange = 1..15
    )

    @SettingKey
    public val horizontalAlignment: EnumSettingObject<HorizontalAlignment> = enum(HorizontalAlignment.Start)

    @SettingKey
    public val lastWorkspaceUsed: StringSettingObject = string("")

    @SettingKey
    public val leftDrawerAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.left_drawer_action,
        default = DrawerActions.defaultLeftDrawerAction
    )

    @SettingKey
    public val rightDrawerAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.right_drawer_action,
        default = DrawerActions.defaultRightDrawerAction
    )

    @SettingKey
    public val leftDrawerWidth: DpSettingObject = dp(
        title = R.string.left_drawer_width,
        default = 0.dp,
        allowedRange = 0.dp..300.dp
    )

    @SettingKey
    public val rightDrawerWidth: DpSettingObject = dp(
        title = R.string.right_drawer_width,
        default = 0.dp,
        allowedRange = 0.dp..300.dp
    )

    @SettingKey
    public val drawerEnterAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.drawer_enter_key_action,
        default = DrawerActions.defaultEnterAction
    )

    @SettingKey
    public val drawerHomeAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.home_action,
        description = R.string.home_action_desc,
        default = DrawerActions.defaultHomeAction
    )

    @SettingKey
    public val scrollDownDrawerAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.scroll_down_action,
        default = DrawerActions.defaultScrollDownAction
    )

    @SettingKey
    public val scrollUpDrawerAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.scroll_up_action,
        default = DrawerActions.defaultScrollUpAction
    )

    @SettingKey
    public val backDrawerAction: EnumSettingObject<DrawerActions> = enum(
        title = R.string.back_action,
        default = DrawerActions.defaultBackAction
    )

    @SettingKey
    public val iconShape: IconShapeSettingObject = shape(
        title = R.string.edit_icons_shape,
        description = R.string.edit_icons_shape_desc,
        default = IconShape.PlatformDefault
    )

    @SettingKey
    public val iconsSpacingHorizontal: DpSettingObject = dp(
        title = R.string.icons_spacing_horizontal,
        description = R.string.icons_spacing_horizontal_desc,
        default = 8.dp,
        allowedRange = 0.dp..50.dp
    )

    @SettingKey
    public val iconsSpacingVertical: DpSettingObject = dp(
        title = R.string.icons_spacing_vertical,
        description = R.string.icons_spacing_vertical_desc,
        default = 8.dp,
        allowedRange = 0.dp..50.dp

    )

    @SettingKey
    public val iconSize: DpSettingObject = dp(
        description = R.string.max_icon_size_desc,
        title = R.string.max_icon_size,
        default = 96.dp,
        allowedRange = 0.dp..200.dp
    )

    @SettingKey
    public val useCategory: BooleanSettingObject = boolean(
        title = R.string.use_categories,
        description = R.string.use_categories_desc,
        default = false
    )

    @SettingKey
    public val categoryGridCells: IntSettingObject = int(
        title = R.string.category_cells,
        description = R.string.category_cells,
        default = 3,
        allowedRange = 2..5
    )

    @SettingKey
    public val showCategoryName: BooleanSettingObject = boolean(
        title = R.string.show_category_name,
        description = R.string.show_category_name_desc,
        default = true
    )

    @SettingKey
    public val showSearchBar: BooleanSettingObject = boolean(
        title = R.string.search_bar,
        default = true
    )

    @SettingKey
    public val showRecentlyUsedApps: BooleanSettingObject = boolean(
        default = false,
        title = R.string.show_recently_used_apps,
        description = R.string.show_recently_used_apps_desc
    )

    @SettingKey
    public val recentlyUsedAppsCount: IntSettingObject = int(
        default = 5,
        title = R.string.recently_used_apps_count,
        description = R.string.recently_used_apps_count_desc,
        allowedRange = 1..20
    )

    @SettingKey
    public val recentlyUsedPackages: StringListSettingObject = stringList(
        default = emptyList(),
        onChanged = {},
        backupable = false
    )

    @SettingKey
    public val pullDownAnimations: BooleanSettingObject = boolean(
        title = R.string.pull_down_animations,
        description = R.string.pull_down_animations_desc,
        default = true
    )

    @SettingKey
    public val pullDownWallPaperDim: BooleanSettingObject = boolean(
        title = R.string.pull_down_wallpaper_dim,
        description = R.string.pull_down_wallpaper_dim_desc,
        default = true
    )

//    @SettingKey
//    val pullDownIconFade = boolean(true)

    @SettingKey
    public val pullDownScaleIn: BooleanSettingObject = boolean(
        title = R.string.pull_down_scale_in,
        description = R.string.pull_down_scale_in_desc,
        default = true
    )

    /**
     * The order of the search bar / recently used in drawer
     */
    @SettingKey
    public val toolbarsOrder: EnumListSettingObject<DrawerToolbar> = enumList(
        title = R.string.toolbars_order,
        default = DrawerToolbar.defaultDrawerToolbarOrder
    )
}