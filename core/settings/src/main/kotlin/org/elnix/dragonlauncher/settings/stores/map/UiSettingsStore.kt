package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.FloatSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.StringSetSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
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
        default = true,
    )

    @SettingKey
    public val showPreviewPoint: BooleanSettingObject = boolean(
        title = R.string.show_launching_app_icon,
        description = R.string.show_launching_app_icon_description,
        default = true
    )

    @SettingKey
    public val appLabelIconOverlayTopPadding: IntSettingObject = int(
        title = R.string.app_label_icon_overlay_top_padding,
        default = 20,
        allowedRange = 0..1000
    )

    @SettingKey
    public val appLabelOverlaySize: IntSettingObject = int(
        title = R.string.app_label_overlay_size,
        default = 18,
        allowedRange = 0..100
    )

    @SettingKey
    public val appIconOverlaySize: IntSettingObject = int(
        title = R.string.app_icon_overlay_size,
        default = 22,
        allowedRange = 0..400
    )

    @SettingKey
    public val fullScreen: BooleanSettingObject = boolean(
        title = R.string.fullscreen_app,
        description = R.string.fullscreen_description,
        default = false
    )

    @SettingKey
    public val autoSeparatePoints: BooleanSettingObject = boolean(
        title = R.string.auto_separate,
        default = true
    )

    @SettingKey
    public val snapPoints: BooleanSettingObject = boolean(
        title = R.string.snap_points,
        default = true
    )

    @SettingKey
    public val snapShapesOffset: BooleanSettingObject = boolean(
        title = R.string.snap_shapes_offset,
        default = true
    )

    @SettingKey
    public val snapShapesScale: BooleanSettingObject = boolean(
        title = R.string.snap_shapes_scale,
        default = false
    )


    @SettingKey
    public val snapShapeAngle: BooleanSettingObject = boolean(
        title = R.string.snap_shapes_angle,
        default = true
    )

    @SettingKey
    public val autoMerge: BooleanSettingObject = boolean(
        title = R.string.auto_merge,
        default = true
    )

    @SettingKey
    public val showPointPreviewCenterStartPosition: BooleanSettingObject = boolean(
        title = R.string.show_app_icon_start_drag_position,
        description = R.string.show_app_icon_start_drag_position_description,
        default = false
    )

    @SettingKey
    public val linePreviewSnapToAction: BooleanSettingObject = boolean(
        title = R.string.line_preview_snap_to_action,
        description = R.string.line_preview_snap_to_action_description,
        default = false
    )

    @SettingKey
    public val animationWhenSnapping: BooleanSettingObject = boolean(
        title = R.string.animation_when_snapping,
        description = R.string.animation_when_snapping_desc,
        default = false
    )

    @SettingKey
    public val showAppLaunchingPreview: BooleanSettingObject = boolean(
        title = R.string.show_app_launch_preview,
        description = R.string.show_app_launch_preview_description,
        default = true
    )

    @SettingKey
    public val showAllActionsOnCurrentShape: BooleanSettingObject = boolean(
        title = R.string.show_all_actions_on_current_shape,
        description = R.string.show_all_actions_on_current_shape_desc,
        default = true
    )

    @SettingKey
    public val showAllActionsOnCurrentNest: BooleanSettingObject = boolean(
        title = R.string.show_all_actions_on_current_nest,
        description = R.string.show_all_actions_on_current_nest_desc,
        default = false
    )

    @SettingKey
    public val wallpaperDimMainScreen: FloatSettingObject = float(
        title = R.string.wallpaper_dim_amount,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    public val wallpaperDimDrawerScreen: FloatSettingObject = float(
        title = R.string.wallpaper_dim_amount,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    public val globalFont: StringSettingObject = string("Default")

    /** How far the points drawing system `actionsInCircle` draws the points */
    @SettingKey
    public val maxNestsDepth: IntSettingObject = int(
        title = R.string.depth,
        description = R.string.depth_desc,
        default = 2,
        allowedRange = 1..10
    )

    /** How many sub live nests can be drawn at once */
    @SettingKey
    public val maxLiveNestsDepth: IntSettingObject = int(
        title = R.string.live_nest_depth,
        description = R.string.live_nests_depth_desc,
        default = 5,
        allowedRange = 1..10
    )

    @SettingKey
    public val mainScreenLayers: StringSettingObject = string(
        title = R.string.main_screen_layers,
        default = ""
    )

    @SettingKey
    public val cellSizeDp: IntSettingObject = int(
        title = R.string.cell_size,
        description = R.string.cell_size_help,
        default = 30,
        allowedRange = 1..100
    )

    @SettingKey
    public val showTooltipsOnAddPointDialog: BooleanSettingObject = boolean(
        default = true,
    )

    @SettingKey
    public val userThemes: StringSetSettingObject = stringSet(emptySet())

    @SettingKey
    public val multiplyOrSubtractOpacityInLiveNests: BooleanSettingObject = boolean(
        title = R.string.multiply_or_subtract_opacity_in_live_nests,
        description = R.string.multiply_or_subtract_opacity_in_live_nests_desc,
        default = true
    )

    @SettingKey
    public val doNotRemindMeAgainPinLockWarning: BooleanSettingObject = boolean(
        title = R.string.do_not_remind_me_again_pin_lock,
        description = R.string.do_not_remind_me_again_pin_lock_desc,
        default = false
    )
}