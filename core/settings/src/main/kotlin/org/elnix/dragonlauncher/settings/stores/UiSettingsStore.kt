package org.elnix.dragonlauncher.settings.stores

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.Settings

object UiSettingsStore : MapSettingsStore() {

    override val name: String = "Ui"
    override val dataStoreName: DataStoreName = DataStoreName.UI

    /*  ─────────────  Use the computing of HSV color to produce a color that depends on the angle / progress  ─────────────  */
    val rgbLoading = Settings.boolean(
        key = "rgbLoading",
        dataStoreName = dataStoreName,
        default = true
    )

    val rgbLine = Settings.boolean(
        key = "rgbLine",
        dataStoreName = dataStoreName,
        default = true
    )


    /*  ─────────────  Overlay on top of the screen  ─────────────  */
    val showLaunchingAppLabel = Settings.boolean(
        key = "showLaunchingAppLabel",
        dataStoreName = dataStoreName,
        default = true,
    )

    val showLaunchingAppIcon = Settings.boolean(
        key = "showLaunchingAppIcon",
        dataStoreName = dataStoreName,
        default = true
    )
    val appLabelIconOverlayTopPadding = Settings.int(
        key = "appLabelIconOverlayTopPadding",
        dataStoreName = dataStoreName,
        default = 30,
        allowedRange = 0..1000
    )

    val appLabelOverlaySize = Settings.int(
        key = "appLabelOverlaySize",
        dataStoreName = dataStoreName,
        default = 18,
        allowedRange = 0..100
    )

    val appIconOverlaySize = Settings.int(
        key = "appIconOverlaySize",
        dataStoreName = dataStoreName,
        default = 22,
        allowedRange = 0..400
    )
    /*  ──────────────────────────  */


    val fullScreen = Settings.boolean(
        key = "fullscreen",
        dataStoreName = dataStoreName,
        default = false
    )


    /* Used in settings screen internally to remember the 2 toggleable button */
    val autoSeparatePoints = Settings.boolean(
        key = "autoSeparatePoints",
        dataStoreName = dataStoreName,
        default = true
    )
    val snapPoints = Settings.boolean(
        key = "snapPoints",
        dataStoreName = dataStoreName,
        default = true
    )
    val freeMoveDraggedPoint = Settings.boolean(
        key = "freeMoveDraggedPoint",
        dataStoreName = dataStoreName,
        default = true
    )



    /*  ───────────── Advanced line preview customization  ─────────────  */
    val showCirclePreview = Settings.boolean(
        key = "showCirclePreview",
        dataStoreName = dataStoreName,
        default = true
    )
    val showAppPreviewIconCenterStartPosition = Settings.boolean(
        key = "showAppPreviewIconCenterStartPosition",
        dataStoreName = dataStoreName,
        default = false
    )
    val linePreviewSnapToAction = Settings.boolean(
        key = "linePreviewSnapToAction",
        dataStoreName = dataStoreName,
        default = false
    )


    /* Show the current selected app on drag in the main screen / show them all on the circle */
    val showAppLaunchingPreview = Settings.boolean(
        key = "showAppLaunchPreview",
        dataStoreName = dataStoreName,
        default = true
    )
    val showAllActionsOnCurrentCircle = Settings.boolean(
        key = "showAllActionsOnCurrentCircle",
        dataStoreName = dataStoreName,
        default = true
    )
    val showAllActionsOnCurrentNest = Settings.boolean(
        key = "showAllActionsOnCurrentNest",
        dataStoreName = dataStoreName,
        default = false
    )


    /*  ───────────── Internal icon packs values ─────────────  */
    val selectedIconPack = Settings.string(
        key = "selected_icon_pack",
        dataStoreName = dataStoreName,
        default = ""
    )
    val iconPackTint = Settings.color(
        key = "icon_pack_tint",
        dataStoreName = dataStoreName,
        default = Color.Unspecified
    )


    /*  ───────────── Wallpaper Things ─────────────  */
    val wallpaperDimMainScreen = Settings.float(
        key = "wallpaperDimMainScreen",
        dataStoreName = dataStoreName,
        default = 0f,
        allowedRange = 0f..1f
    )

    val wallpaperDimDrawerScreen = Settings.float(
        key = "wallpaperDimDrawerScreen",
        dataStoreName = dataStoreName,
        default = 0f,
        allowedRange = 0f..1f
    )

    val globalFont = Settings.string(
        key = "globalFont",
        dataStoreName = dataStoreName,
        default = "Default"
    )

    /** How far the points drawing system `actionsInCircle` draws the points */
    val maxNestsDepth = Settings.int(
        key = "maxNestsDepth",
        dataStoreName = dataStoreName,
        default = 2,
        allowedRange = 1..10
    )


    /** How many sub live nests can be drawn at once */
    val maxLiveNestsDepth = Settings.int(
        key = "maxLiveNestsDepth",
        dataStoreName = dataStoreName,
        default = 5,
        allowedRange = 1..10
    )

    /**
     * Whether to use my custom-made color schemes for objects, or the default Android colors schemes.
     * For ex: my switch uses no borders, and other colors channels than the default one, while the android one has borders
     * */
    val useCustomColorChannels = Settings.boolean(
        key = "useCustomColorChannels",
        dataStoreName = dataStoreName,
        default = true
    )

    val mainScreenLayers = Settings.string(
        key = "mainScreenLayers",
        dataStoreName = dataStoreName,
        default = ""
    )

    val cellSizeDp = Settings.int(
        key = "cellSizeDp",
        dataStoreName = dataStoreName,
        default = 30,
        allowedRange = 1..100
    )

    val showTooltipsOnAddPointDialog = Settings.boolean(
        key = "showTooltipsOnAddPointDialog",
        dataStoreName = dataStoreName,
        default = true,
    )

    val userThemes = Settings.stringSet(
        key = "userThemes",
        dataStoreName = ColorModesSettingsStore.dataStoreName,
        default = emptySet()
    )

    val multiplyOrSubtractOpacityInLiveNests = Settings.boolean(
        key = "multiplyOrSubtractOpacityInLiveNests",
        dataStoreName = dataStoreName,
        default = true
    )

    // unsing explicit this to avoid other stores that have the same name keys to be imported by mistake
    override val ALL: List<BaseSettingObject<*, *>> = listOf(
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
        this.selectedIconPack,
        this.iconPackTint,
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
        this.multiplyOrSubtractOpacityInLiveNests
    )
}
