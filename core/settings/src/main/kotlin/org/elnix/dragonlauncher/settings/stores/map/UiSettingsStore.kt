package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.dp
import io.github.elnix90.core.objects.float
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.objects.stringSet
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object UiSettingsStore : MapSettingsStore() {
	/** Overlay on top of the screen */
	@SettingKey
	val showLaunchingAppLabel =
		boolean(
			title = R.string.show_launching_app_label,
			description = R.string.show_launching_app_label_description,
			icon = R.drawable.text_fields_alt,
			default = true
		)

	@SettingKey
	val showLaunchingAppIcon =
		boolean(
			title = R.string.show_launching_app_icon,
			description = R.string.show_launching_app_icon_description,
			icon = R.drawable.apps,
			default = true
		)

	/** Show the current selected point */
	@SettingKey
	val showCurrentSelectedPoint =
		boolean(
			title = R.string.show_current_selected_point,
			description = R.string.show_current_selected_point_desc,
			icon = R.drawable.visibility,
			default = true
		)

	@SettingKey
	val appLabelIconOverlayTopPadding =
		dp(
			title = R.string.app_label_icon_overlay_top_padding,
			description = R.string.app_label_icon_overlay_top_padding_desc,
			icon = R.drawable.height,
			default = 50.dp,
			allowedRange = 0.dp..1000.dp
		)

	@SettingKey
	val appLabelOverlaySize =
		int(
			title = R.string.app_label_overlay_size,
			description = R.string.self_explanatory,
			icon = R.drawable.format_size,
			default = 18,
			allowedRange = 0..100
		)

	@SettingKey
	val appIconOverlaySize =
		dp(
			title = R.string.app_icon_overlay_size,
			description = R.string.self_explanatory,
			icon = R.drawable.format_size,
			default = 22.dp,
			allowedRange = 0.dp..400.dp
		)

	@SettingKey
	val multiSelectPoints =
		boolean(
			title = R.string.multi_select_points,
			description = R.string.multi_select_points_desc,
			icon = R.drawable.app_registration,
			default = false
		)

	@SettingKey
	val fullScreen =
		boolean(
			title = R.string.fullscreen_app,
			description = R.string.fullscreen_description,
			icon = R.drawable.fullscreen,
			default = false
		)

	@SettingKey
	val showPointPreviewCenterStartPosition =
		boolean(
			title = R.string.show_app_icon_start_drag_position,
			description = R.string.show_app_icon_start_drag_position_description,
			icon = R.drawable.center_focus_strong,
			default = false
		)

	@SettingKey
	val linePreviewSnapToAction =
		boolean(
			title = R.string.line_preview_snap_to_action,
			description = R.string.line_preview_snap_to_action_description,
			icon = R.drawable.flash_auto,
			default = false
		)

	@SettingKey
	val animationWhenSnapping =
		boolean(
			title = R.string.animation_when_snapping,
			description = R.string.animation_when_snapping_desc,
			icon = R.drawable.animation,
			default = false
		)

	@SettingKey
	val showAllPointsInCurrentShape =
		boolean(
			title = R.string.show_all_actions_on_current_shape,
			description = R.string.show_all_actions_on_current_shape_desc,
			icon = R.drawable.shape_line,
			default = true
		)

	@SettingKey
	val showAllPointsInCurrentNest =
		boolean(
			title = R.string.show_all_actions_in_current_nest,
			description = R.string.show_all_actions_in_current_nest_desc,
			icon = R.drawable.select_all,
			default = false
		)

	@SettingKey
	val showCurrentShape =
		boolean(
			title = R.string.show_shape,
			description = R.string.show_shape_desc,
			icon = R.drawable.shapes,
			default = true
		)

	@SettingKey
	val showAllShapesInNest =
		boolean(
			title = R.string.show_all_shapes,
			description = R.string.show_all_shapes_desc,
			icon = R.drawable.all_inclusive,
			default = false
		)

	@SettingKey
	val wallpaperDimMainScreen =
		float(
			title = R.string.wallpaper_dim_amount_main,
			description = R.string.dim_amount_help,
			icon = R.drawable.wallpaper,
			default = 0f,
			allowedRange = 0f..1f
		)

	@SettingKey
	val wallpaperDimDrawerScreen =
		float(
			title = R.string.wallpaper_dim_amount_drawer,
			description = R.string.dim_amount_help,
			icon = R.drawable.wallpaper,
			default = 0f,
			allowedRange = 0f..1f
		)

	@SettingKey
	val pointsScreensTransparency =
		float(
			title = R.string.points_screens_transparency,
			description = R.string.points_screens_transparency_desc,
			icon = R.drawable.opacity,
			default = 0.5f,
			allowedRange = 0f..1f
		)

	@SettingKey
	val globalFont = string("Default")

	/** How far the points drawing system `actionsInCircle` draws the points */
	@SettingKey
	val maxNestsDepth =
		int(
			title = R.string.depth,
			description = R.string.depth_desc,
			icon = R.drawable.height,
			default = 2,
			allowedRange = 1..5
		)

	/** How many sub live nests can be drawn at once */
	@SettingKey
	val maxLiveNestsDepth =
		int(
			title = R.string.live_nest_depth,
			description = R.string.live_nests_depth_desc,
			icon = R.drawable.height,
			default = 5,
			allowedRange = 1..10
		)

	@SettingKey
	val showGridWhenSnappingIsOn =
		boolean(
			title = R.string.show_grid,
			description = R.string.show_grid_when_snapping_is_on,
			icon = R.drawable.grid_on,
			default = true
		)

	@SettingKey
	val nestsCellSizeDp =
		dp(
			title = R.string.nests_cell_size,
			description = R.string.nests_cell_size_desc,
			icon = R.drawable.resize,
			default = 30.dp,
			allowedRange = 1.dp..100.dp
		)

	@SettingKey
	val pointsCellSizeDp =
		dp(
			title = R.string.points_cell_size,
			description = R.string.points_cell_size_desc,
			icon = R.drawable.resize,
			default = 30.dp,
			allowedRange = 1.dp..100.dp
		)

	@SettingKey
	val widgetsCellSizeDp =
		dp(
			title = R.string.widget_cell_size,
			description = R.string.widget_cell_size_help,
			icon = R.drawable.resize,
			default = 30.dp,
			allowedRange = 1.dp..100.dp
		)

	@SettingKey
	val userThemes = stringSet(emptySet())

	@SettingKey
	val multiplyOrSubtractOpacityInLiveNests =
		boolean(
			title = R.string.multiply_or_subtract_opacity_in_live_nests,
			description = R.string.multiply_or_subtract_opacity_in_live_nests_desc,
			icon = R.drawable.opacity,
			default = true
		)

	@SettingKey
	val doNotRemindMeAgainPinLockWarning =
		boolean(
			title = R.string.do_not_remind_me_again_pin_lock,
			description = R.string.do_not_remind_me_again_pin_lock_desc,
			icon = R.drawable.lock_open,
			default = false
		)

	/**
	 * Point settings screen settings, only used in this screen
	 */
	@SettingKey
	val autoSeparatePoints =
		boolean(
			title = R.string.auto_separate,
			description = R.string.auto_separate_desc,
			icon = null, // TODO
			default = true
		)

	@SettingKey
	val snapPoints =
		boolean(
			title = R.string.snap_points,
			description = R.string.snap_points_desc,
			icon = R.drawable.grid_guides,
			default = true
		)

	@SettingKey
	val snapPointsToShapes =
		boolean(
			title = R.string.snap_points_to_shapes,
			description = R.string.snap_points_to_shapes_desc,
			icon = R.drawable.grid_guides,
			default = true
		)

	@SettingKey
	val snapPointsAngle =
		boolean(
			title = R.string.snap_points_angle,
			description = R.string.snap_points_angle_desc,
			icon = R.drawable.grid_guides,
			default = true
		)

	@SettingKey
	val snapPointAngleThreshold =
		int(
			title = R.string.snap_points_angle_threshold,
			description = R.string.snap_points_angle_threshold_desc,
			icon = R.drawable.arrow_right,
			default = 15,
			allowedRange = 1..180
		)

	@SettingKey
	val showSnapPointAngleLines =
		boolean(
			title = R.string.show_snap_point_angle_lines,
			description = R.string.show_snap_point_angle_lines_desc,
			icon = R.drawable.visibility,
			default = false
		)

	@SettingKey
	val allowFreePoints =
		boolean(
			title = R.string.allow_free_points,
			description = R.string.allow_free_points_desc,
			icon = R.drawable.lock_open,
			default = false
		)

	@SettingKey
	val snapShapesOffset =
		boolean(
			title = R.string.snap_shapes_offset,
			icon = R.drawable.grid_guides,
			default = true
		)

	@SettingKey
	val snapShapesCenter =
		boolean(
			title = R.string.snap_shapes_center,
			icon = R.drawable.center_focus_strong,
			default = true
		)

// 	@SettingKey
// 	val snapShapesScale =
// 		boolean(
// 			title = R.string.snap_shapes_scale,
// 			icon = R.drawable.text_fields_alt,
// 			default = false
// 		)

	@SettingKey
	val snapShapeAngle =
		boolean(
			title = R.string.snap_shapes_angle,
			icon = R.drawable.trhee_d_rotation,
			default = false
		)

	@SettingKey
	val autoMerge =
		boolean(
			title = R.string.auto_merge,
			description = R.string.auto_merge_desc,
			icon = R.drawable.merge,
			default = true
		)
}
