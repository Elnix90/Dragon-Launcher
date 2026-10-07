package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.objects.dp
import io.github.elnix90.core.objects.enum
import io.github.elnix90.core.objects.enumList
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.objects.stringList
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.enumsui.select.DrawerAlign
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.base.model.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.theme.AmoledDragonColorScheme
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.specialObjects.shape

@SettingsStore
object DrawerSettingsStore : MapSettingsStore() {
	@SettingKey
	val autoOpenSingleMatch =
		boolean(
			title = R.string.auto_launch_single_match,
			description = R.string.auto_launch_single_match_desc,
			icon = R.drawable.open_in_new,
			default = true
		)

	@SettingKey
	val disableAutoLaunchWhenFirstCharIs =
		string(
			title = R.string.disable_auto_launch_when_first_char_is,
			description = R.string.pick_a_character,
			icon = R.drawable.motion_photos,
			default = " "
		)

	@SettingKey
	val searchAllWorkspacesOnlyWhenFirstCharIs =
		string(
			title = R.string.search_all_workspaces_when_first_char_is,
			description = R.string.pick_a_character,
			icon = R.drawable.workspaces,
			default = "."
		)

	@SettingKey
	val showAppIconsInDrawer =
		boolean(
			title = R.string.show_app_icons_in_drawer,
			description = R.string.show_app_icons_in_drawer_desc,
			icon = R.drawable.apps,
			default = true
		)

	@SettingKey
	val showAppLabelsInDrawer =
		boolean(
			title = R.string.show_app_labels_in_drawer,
			description = R.string.show_app_labels_in_drawer_desc,
			icon = R.drawable.text_fields_alt,
			default = true
		)

	@SettingKey
	val labelTextColor =
		color(
			title = R.string.drawer_label_color,
			description = R.string.drawer_label_color_desc,
			icon = R.drawable.text_fields_alt,
			default = Color.Unspecified
		)

	@SettingKey
	val autoShowKeyboardOnDrawer =
		boolean(
			title = R.string.auto_show_keyboard,
			description = R.string.auto_show_keyboard_desc,
			icon = R.drawable.keyboard,
			default = true
		)

	@SettingKey
	val autoAskToUnlockProfile =
		boolean(
			title = R.string.auto_show_unlock_profile,
			description = R.string.auto_show_unlock_profile_desc,
			icon = R.drawable.lock_open,
			default = true
		)

	@SettingKey
	val gridSize =
		int(
			title = R.string.grid_size,
			description = R.string.grid_size_desc,
			icon = R.drawable.grid_on,
			default = 5,
			allowedRange = 1..15
		)

	@SettingKey
	val horizontalAlignment = enum(HorizontalAlignment.Start)

	@SettingKey
	val lastWorkspaceUsed =
		string(
			default = "",
			backupable = false
		)

	@SettingKey
	val tapEmptySpaceAction =
		enum(
			title = R.string.tap_empty_space_action,
			default = DrawerActions.Close
		)

	@SettingKey
	val leftDrawerAction =
		enum(
			title = R.string.left_drawer_action,
			default = DrawerActions.defaultLeftDrawerAction
		)

	@SettingKey
	val rightDrawerAction =
		enum(
			title = R.string.right_drawer_action,
			default = DrawerActions.defaultRightDrawerAction
		)

	@SettingKey
	val leftDrawerWidth =
		dp(
			title = R.string.left_drawer_width,
			default = 0.dp,
			allowedRange = 0.dp..300.dp
		)

	@SettingKey
	val rightDrawerWidth =
		dp(
			title = R.string.right_drawer_width,
			default = 0.dp,
			allowedRange = 0.dp..300.dp
		)

	@SettingKey
	val drawerEnterAction =
		enum(
			title = R.string.drawer_enter_key_action,
			default = DrawerActions.defaultEnterAction
		)

	@SettingKey
	val drawerHomeAction =
		enum(
			title = R.string.home_action,
			description = R.string.home_action_desc,
			default = DrawerActions.defaultHomeAction
		)

	@SettingKey
	val drawerScrollDownAction =
		enum(
			title = R.string.scroll_down_action,
			default = DrawerActions.defaultScrollDownAction
		)

	@SettingKey
	val drawerScrollUpAction =
		enum(
			title = R.string.scroll_up_action,
			default = DrawerActions.defaultScrollUpAction
		)

	@SettingKey
	val drawerClickSearchIconAction =
		enum(
			title = R.string.click_search_icon_action,
			default = DrawerActions.defaultClickSearchAction
		)

	@SettingKey
	val drawerBackAction =
		enum(
			title = R.string.back_action,
			default = DrawerActions.defaultBackAction
		)

	@SettingKey
	val iconShape =
		shape(
			title = R.string.edit_icons_shape,
			description = R.string.edit_icons_shape_desc,
			icon = R.drawable.shapes,
			default = IconShape.PlatformDefault
		)

	@SettingKey
	val iconsSpacingHorizontal =
		dp(
			title = R.string.icons_spacing_horizontal,
			description = R.string.icons_spacing_horizontal_desc,
			icon = R.drawable.more_horiz,
			default = 0.dp,
			allowedRange = 0.dp..50.dp
		)

	@SettingKey
	val iconsSpacingVertical =
		dp(
			title = R.string.icons_spacing_vertical,
			description = R.string.icons_spacing_vertical_desc,
			icon = R.drawable.more_vert,
			default = 8.dp,
			allowedRange = 0.dp..50.dp
		)

	@SettingKey
	val iconSize =
		dp(
			description = R.string.icon_size_desc,
			title = R.string.icon_size,
			icon = R.drawable.format_size,
			default = 48.dp,
			allowedRange = 0.dp..200.dp
		)

	@SettingKey
	val useCategory =
		boolean(
			title = R.string.use_categories,
			description = R.string.use_categories_desc,
			icon = R.drawable.fullscreen,
			default = false
		)

	@SettingKey
	val categoryGridCells =
		int(
			title = R.string.category_grid_cells,
			description = R.string.category_grid_cells_desc,
			icon = R.drawable.more_horiz,
			default = 3,
			allowedRange = 2..5
		)

	@SettingKey
	val categoryCells =
		int(
			title = R.string.category_cells,
			description = R.string.category_cells_desc,
			icon = R.drawable.more_horiz,
			default = 3,
			allowedRange = 1..4
		)

	@SettingKey
	val showCategoryName =
		boolean(
			title = R.string.show_category_name,
			description = R.string.show_category_name_desc,
			icon = R.drawable.text_fields_alt,
			default = true
		)

	@SettingKey
	val showSearchBar =
		boolean(
			title = R.string.search_bar,
			default = true
		)

	@SettingKey
	val showRecentlyUsedApps =
		boolean(
			title = R.string.show_recently_used_apps,
			description = R.string.show_recently_used_apps_desc,
			icon = R.drawable.recent,
			default = false
		)

	@SettingKey
	val recentlyUsedAppsCount =
		int(
			default = 5,
			title = R.string.recently_used_apps_count,
			description = R.string.recently_used_apps_count_desc,
			icon = R.drawable.recent,
			allowedRange = 1..20
		)

	@SettingKey
	val recentlyUsedPackages =
		stringList(
			default = emptyList(),
			onChanged = {},
			backupable = false
		)

	@SettingKey
	val pullDownAnimations =
		boolean(
			title = R.string.pull_down_animations,
			description = R.string.pull_down_animations_desc,
			icon = R.drawable.animation,
			default = true
		)

	@SettingKey
	val pullDownWallPaperDim =
		boolean(
			title = R.string.pull_down_wallpaper_dim,
			description = R.string.pull_down_wallpaper_dim_desc,
			icon = R.drawable.wallpaper,
			default = true
		)

//    @SettingKey
//    val pullDownIconFade = boolean(true)

	@SettingKey
	val pullDownScaleIn =
		boolean(
			title = R.string.pull_down_scale_in,
			description = R.string.pull_down_scale_in_desc,
			icon = R.drawable.format_size,
			default = true
		)

	/**
	 * The order of the search bar / recently used in drawer
	 */
	@SettingKey
	val toolbarsOrder =
		enumList(
			title = R.string.toolbars_order,
			icon = R.drawable.drag_indicator,
			default = DrawerToolbar.defaultToolbarOrder
		)

	@SettingKey
	val disabledSystemCategories =
		stringList(
			title = R.string.disabled_system_categories,
			description = R.string.disabled_system_categories_desc,
			icon = R.drawable.filter_alt,
			default = emptyList()
		)

	@SettingKey
	val categoryOrder =
		stringList(
			default = emptyList(),
			backupable = false
		)

	@SettingKey
	val categoryColor =
		color(
			default = AmoledDragonColorScheme.surfaceVariant,
			title = R.string.category_color
		)

	@SettingKey
	val drawerAlign =
		enum(
			default = DrawerAlign.Top,
			title = R.string.drawer_align,
			description = R.string.drawer_align_desc
		)

	@SettingKey
	val imePadding =
		boolean(
			title = R.string.ime_padding_drawer,
			description = R.string.ime_padding_drawer_desc,
			icon = R.drawable.keyboard,
			default = false
		)

	@SettingKey
	val recentlyInstalledAppsDuration =
		int(
			title = R.string.recently_installed_apps_duration,
			description = R.string.recently_installed_apps_duration_desc,
			icon = R.drawable.recent,
			default = 2,
			allowedRange = 0..10
		)
}
