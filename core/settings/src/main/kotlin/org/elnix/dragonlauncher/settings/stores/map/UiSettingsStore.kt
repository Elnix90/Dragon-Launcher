package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.FloatSettingObject.Companion.float
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSetSettingObject.Companion.stringSet
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object UiSettingsStore : MapSettingsStore(DataStoreName.UI) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.rgbLoading,
            this.rgbLine,
            this.showLaunchingAppLabel,
            this.showLaunchingAppIcon,
            this.showAppLaunchingPreview,
            this.fullScreen,
            this.showCirclePreview,
            this.snapPoints,
            this.autoSeparatePoints,
            this.freeMoveDraggedPoint,
            this.showAppPreviewIconCenterStartPosition,
            this.linePreviewSnapToAction,
            this.showAllActionsOnCurrentCircle,
            this.showAllActionsOnCurrentNest,
            this.appLabelIconOverlayTopPadding,
            this.appLabelOverlaySize,
            this.appIconOverlaySize,
            this.wallpaperDimMainScreen,
            this.wallpaperDimDrawerScreen,
            this.globalFont,
            this.maxNestsDepth,
            this.maxLiveNestsDepth,
            this.useCustomColorChannels,
            this.mainScreenLayers,
            this.cellSizeDp,
            this.showTooltipsOnAddPointDialog,
            this.userThemes,
            this.multiplyOrSubtractOpacityInLiveNests,
            this.doNotRemindMeAgainPinLockWarning
        )

    /** Use the computing of HSV color to produce a color that depends on the angle / progress */
    val rgbLoading by boolean(
        title = R.string.rgb_loading_settings,
        description = R.string.rgb_loading_description,
        default = true
    )

    /** Use the computing of HSV color to produce a color that depends on the angle / progress */
    val rgbLine by boolean(
        title = R.string.rgb_line_selector,
        description = R.string.rgb_line_selector_description,
        default = true
    )

    /** Overlay on top of the screen */
    val showLaunchingAppLabel by boolean(
        title = R.string.show_launching_app_label,
        description = R.string.show_launching_app_label_description,
        default = true,
    )

    val showLaunchingAppIcon by boolean(
        title = R.string.show_launching_app_icon,
        description = R.string.show_launching_app_icon_description,
        default = true
    )
    val appLabelIconOverlayTopPadding by int(
        title = R.string.app_label_icon_overlay_top_padding,
        description = null,
        default = 20,
        allowedRange = 0..1000
    )

    val appLabelOverlaySize by int(
        title = R.string.app_label_overlay_size,
        description = null,
        default = 18,
        allowedRange = 0..100
    )

    val appIconOverlaySize by int(
        title = R.string.app_icon_overlay_size,
        description = null,
        default = 22,
        allowedRange = 0..400
    )

    val fullScreen by boolean(
        title = R.string.fullscreen_app,
        description = R.string.fullscreen_description,
        default = false
    )

    /** Whether if the points are separated automatically to avoid overlapping when moving them in the circle */
    val autoSeparatePoints by boolean(
        title = R.string.auto_separate,
        description = null,
        default = true
    )
    val snapPoints by boolean(
        title = R.string.snap_points,
        description = null,
        default = true
    )
    val freeMoveDraggedPoint by boolean(
        title = R.string.free_move_dragged_point,
        description = null,
        default = true
    )

    /** Global setting, can be changed individually for each nest */
    val showCirclePreview by boolean(
        title = R.string.show_app_circle_preview,
        description = R.string.show_app_circle_preview_description,
        default = true
    )
    val showAppPreviewIconCenterStartPosition by boolean(
        title = R.string.show_app_icon_start_drag_position,
        description = R.string.show_app_icon_start_drag_position_description,
        default = false
    )
    val linePreviewSnapToAction by boolean(
        title = R.string.line_preview_snap_to_action,
        description = R.string.line_preview_snap_to_action_description,
        default = false
    )

    /** Show the current selected app on drag in the main screen / show them all on the circle */
    val showAppLaunchingPreview by boolean(
        title = R.string.show_app_launch_preview,
        description = R.string.show_app_launch_preview_description,
        default = true
    )
    val showAllActionsOnCurrentCircle by boolean(
        title = R.string.show_all_actions_on_current_circle,
        description = R.string.show_all_actions_on_current_circle_description,
        default = true
    )
    val showAllActionsOnCurrentNest by boolean(
        title = R.string.show_all_actions_on_current_nest,
        description = R.string.show_all_actions_on_current_nest_desc,
        default = false
    )

    val wallpaperDimMainScreen by float(
        title = R.string.wallpaper_dim_amount,
        description = null,
        default = 0f,
        allowedRange = 0f..1f
    )

    val wallpaperDimDrawerScreen by float(
        title = R.string.wallpaper_dim_amount,
        description = null,
        default = 0f,
        allowedRange = 0f..1f
    )

    val globalFont by string(
        title = null,
        description = null,
        default = "Default"
    )

    /** How far the points drawing system `actionsInCircle` draws the points */
    val maxNestsDepth by int(
        title = R.string.depth,
        description = R.string.depth_desc,
        default = 2,
        allowedRange = 1..10
    )

    /** How many sub live nests can be drawn at once */
    val maxLiveNestsDepth by int(
        title = R.string.live_nest_depth,
        description = R.string.live_nests_depth_desc,
        default = 5,
        allowedRange = 1..10
    )

    /**
     * Whether to use my custom-made color schemes for objects, or the default Android colors schemes.
     * For ex: my switch uses no borders, and other colors channels than the default one, while the android one has borders
     * */
    val useCustomColorChannels by boolean(
        title = R.string.use_custom_color_channels,
        description = R.string.use_custom_color_channels_desc,
        default = true
    )

    val mainScreenLayers by string(
        title = R.string.main_screen_layers,
        description = null,
        default = ""
    )

    val cellSizeDp by int(
        title = R.string.cell_size,
        description = R.string.cell_size_help,
        default = 30,
        allowedRange = 1..100
    )

    val showTooltipsOnAddPointDialog by boolean(
        title = null,
        description = null,
        default = true,
    )

    val userThemes by stringSet(
        title = null,
        description = null,
        default = emptySet()
    )

    val multiplyOrSubtractOpacityInLiveNests by boolean(
        title = R.string.multiply_or_subtract_opacity_in_live_nests,
        description = R.string.multiply_or_subtract_opacity_in_live_nests_desc,
        default = true
    )

    val doNotRemindMeAgainPinLockWarning by boolean(
        title = R.string.do_not_remind_me_again_pin_lock,
        description = R.string.do_not_remind_me_again_pin_lock_desc,
        default = false
    )
}