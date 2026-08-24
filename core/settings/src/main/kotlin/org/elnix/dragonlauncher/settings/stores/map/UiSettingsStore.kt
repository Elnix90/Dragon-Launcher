package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.DpSettingObject
import io.github.elnix90.core.objects.FloatSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.StringSetSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.dp
import io.github.elnix90.core.objects.float
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.objects.stringSet
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object UiSettingsStore : MapSettingsStore() {

    /** Overlay on top of the screen */
    @SettingKey
    public val showLaunchingAppLabel: BooleanSettingObject = boolean(
        title = R.string.show_launching_app_label,
        description = R.string.show_launching_app_label_description,
        icon = R.drawable.text_fields_alt,
        default = true,
    )

    @SettingKey
    public val showLaunchingAppIcon: BooleanSettingObject = boolean(
        title = R.string.show_launching_app_icon,
        description = R.string.show_launching_app_icon_description,
        icon = R.drawable.apps,
        default = true
    )

    @SettingKey
    public val appLabelIconOverlayTopPadding: DpSettingObject= dp(
        title = R.string.app_label_icon_overlay_top_padding,
        description = R.string.app_label_icon_overlay_top_padding_desc,
        icon = R.drawable.height,
        default = 50.dp,
        allowedRange = 0.dp..1000.dp
    )

    @SettingKey
    public val appLabelOverlaySize: IntSettingObject = int(
        title = R.string.app_label_overlay_size,
        description = R.string.self_explanatory,
        icon = R.drawable.format_size,
        default = 18,
        allowedRange = 0..100
    )

    @SettingKey
    public val appIconOverlaySize: DpSettingObject = dp(
        title = R.string.app_icon_overlay_size,
        description = R.string.self_explanatory,
        icon = R.drawable.format_size,
        default = 22.dp,
        allowedRange = 0.dp..400.dp
    )

    @SettingKey
    public val multiSelectPoints: BooleanSettingObject = boolean(
        title = R.string.multi_select_points,
        description = R.string.multi_select_points_desc,
        icon = R.drawable.app_registration,
        default = false,
    )

    @SettingKey
    public val fullScreen: BooleanSettingObject = boolean(
        title = R.string.fullscreen_app,
        description = R.string.fullscreen_description,
        icon = R.drawable.fullscreen,
        default = false
    )

    @SettingKey
    public val showPointPreviewCenterStartPosition: BooleanSettingObject = boolean(
        title = R.string.show_app_icon_start_drag_position,
        description = R.string.show_app_icon_start_drag_position_description,
        icon = R.drawable.center_focus_strong,
        default = false
    )

    @SettingKey
    public val linePreviewSnapToAction: BooleanSettingObject = boolean(
        title = R.string.line_preview_snap_to_action,
        description = R.string.line_preview_snap_to_action_description,
        icon = R.drawable.flash_auto,
        default = false
    )

    @SettingKey
    public val animationWhenSnapping: BooleanSettingObject = boolean(
        title = R.string.animation_when_snapping,
        description = R.string.animation_when_snapping_desc,
        icon = R.drawable.animation,
        default = false
    )

    @SettingKey
    public val showAppLaunchingPreview: BooleanSettingObject = boolean(
        title = R.string.show_app_launch_preview,
        description = R.string.show_app_launch_preview_desc,
        icon = R.drawable.visibility,
        default = true
    )

    @SettingKey
    public val showAllPointsInCurrentShape: BooleanSettingObject = boolean(
        title = R.string.show_all_actions_on_current_shape,
        description = R.string.show_all_actions_on_current_shape_desc,
        icon = R.drawable.shape_line,
        default = true
    )

    @SettingKey
    public val showAllPointsInCurrentNest: BooleanSettingObject = boolean(
        title = R.string.show_all_actions_in_current_nest,
        description = R.string.show_all_actions_in_current_nest_desc,
        icon = R.drawable.select_all,
        default = false
    )

    @SettingKey
    public val showCurrentShape: BooleanSettingObject = boolean(
        title = R.string.show_shape,
        description = R.string.show_shape_desc,
        icon = R.drawable.shapes,
        default = true
    )

    @SettingKey
    public val showAllShapesInNest: BooleanSettingObject = boolean(
        title = R.string.show_all_shapes,
        description = R.string.show_all_shapes_desc,
        icon = R.drawable.all_inclusive,
        default = false
    )

    @SettingKey
    public val wallpaperDimMainScreen: FloatSettingObject = float(
        title = R.string.wallpaper_dim_amount_main,
        description = R.string.dim_amount_help,
        icon = R.drawable.wallpaper,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    public val wallpaperDimDrawerScreen: FloatSettingObject = float(
        title = R.string.wallpaper_dim_amount_drawer,
        description = R.string.dim_amount_help,
        icon = R.drawable.wallpaper,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    public val pointsScreensTransparency: FloatSettingObject = float(
        title = R.string.points_screens_transparency,
        description = R.string.points_screens_transparency_desc,
        icon = R.drawable.opacity,
        default = 0.5f,
        allowedRange = 0f..1f
    )


    @SettingKey
    public val globalFont: StringSettingObject = string("Default")

    /** How far the points drawing system `actionsInCircle` draws the points */
    @SettingKey
    public val maxNestsDepth: IntSettingObject = int(
        title = R.string.depth,
        description = R.string.depth_desc,
        icon = R.drawable.height,
        default = 2,
        allowedRange = 1..5
    )

    /** How many sub live nests can be drawn at once */
    @SettingKey
    public val maxLiveNestsDepth: IntSettingObject = int(
        title = R.string.live_nest_depth,
        description = R.string.live_nests_depth_desc,
        icon = R.drawable.height,
        default = 5,
        allowedRange = 1..10
    )

    @SettingKey
    public val showGridWhenSnappingIsOn: BooleanSettingObject = boolean(
        title = R.string.show_grid,
        description = R.string.show_grid_when_snapping_is_on,
        icon = R.drawable.grid_on,
        default = true
    )

    @SettingKey
    public val nestsCellSizeDp: DpSettingObject = dp(
        title = R.string.nests_cell_size,
        description = R.string.nests_cell_size_desc,
        icon = R.drawable.resize,
        default = 30.dp,
        allowedRange = 1.dp..100.dp
    )

    @SettingKey
    public val pointsCellSizeDp: DpSettingObject = dp(
        title = R.string.points_cell_size,
        description = R.string.points_cell_size_desc,
        icon = R.drawable.resize,
        default = 30.dp,
        allowedRange = 1.dp..100.dp
    )

    @SettingKey
    public val widgetsCellSizeDp: DpSettingObject = dp(
        title = R.string.widget_cell_size,
        description = R.string.widget_cell_size_help,
        icon = R.drawable.resize,
        default = 30.dp,
        allowedRange = 1.dp..100.dp
    )

    @SettingKey
    public val userThemes: StringSetSettingObject = stringSet(emptySet())

    @SettingKey
    public val multiplyOrSubtractOpacityInLiveNests: BooleanSettingObject = boolean(
        title = R.string.multiply_or_subtract_opacity_in_live_nests,
        description = R.string.multiply_or_subtract_opacity_in_live_nests_desc,
        icon = R.drawable.opacity,
        default = true
    )

    @SettingKey
    public val doNotRemindMeAgainPinLockWarning: BooleanSettingObject = boolean(
        title = R.string.do_not_remind_me_again_pin_lock,
        description = R.string.do_not_remind_me_again_pin_lock_desc,
        icon = R.drawable.lock_open,
        default = false
    )

    @SettingKey
    public val useAppEvenIfSignatureIsNotMatched: BooleanSettingObject = boolean(false)


    /**
     * Point settings screen settings, only used in this screen
     */

    @SettingKey
    public val autoSeparatePoints: BooleanSettingObject = boolean(
        title = R.string.auto_separate,
        description = R.string.auto_separate_desc,
        icon = null, // TODO
        default = true
    )

    @SettingKey
    public val snapPoints: BooleanSettingObject = boolean(
        title = R.string.snap_points,
        description = R.string.snap_points_desc,
        icon = R.drawable.grid_guides,
        default = true
    )

    @SettingKey
    public val snapPointsToShapes: BooleanSettingObject = boolean(
        title = R.string.snap_points_to_shapes,
        description = R.string.snap_points_to_shapes_desc,
        icon = R.drawable.grid_guides,
        default = true
    )

    @SettingKey
    public val snapPointsAngle: BooleanSettingObject = boolean(
        title = R.string.snap_points_angle,
        description = R.string.snap_points_angle_desc,
        icon = R.drawable.grid_guides,
        default = true
    )

    @SettingKey
    public val allowFreePoints: BooleanSettingObject = boolean(
        title = R.string.allow_free_points,
        description = R.string.allow_free_points_desc,
        icon = R.drawable.lock_open,
        default = false
    )

    @SettingKey
    public val snapShapesOffset: BooleanSettingObject = boolean(
        title = R.string.snap_shapes_offset,
        icon = R.drawable.grid_guides,
        default = true
    )

    @SettingKey
    public val snapShapesCenter: BooleanSettingObject = boolean(
        title = R.string.snap_shapes_center,
        icon = R.drawable.center_focus_strong,
        default = true
    )

    @SettingKey
    public val snapShapesScale: BooleanSettingObject = boolean(
        title = R.string.snap_shapes_scale,
        icon = R.drawable.text_fields_alt,
        default = false
    )

    @SettingKey
    public val snapShapeAngle: BooleanSettingObject = boolean(
        title = R.string.snap_shapes_angle,
        icon = R.drawable.trhee_d_rotation,
        default = false
    )

    @SettingKey
    public val autoMerge: BooleanSettingObject = boolean(
        title = R.string.auto_merge,
        description = R.string.auto_merge_desc,
        icon = R.drawable.merge,
        default = true
    )
}