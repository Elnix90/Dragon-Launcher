package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.DpSettingObject.Companion.dp
import org.elnix.dragonlauncher.settings.bases.objects.EnumListSettingObject.Companion.enumList
import org.elnix.dragonlauncher.settings.bases.objects.EnumSettingObject.Companion.enum
import org.elnix.dragonlauncher.settings.bases.objects.IconShapeSettingObject.Companion.shape
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSetSettingObject.Companion.stringSet
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object DrawerSettingsStore : MapSettingsStore(DataStoreName.DRAWER) {


    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
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
            this.iconsShape,
            this.iconsSpacingVertical,
            this.iconsSpacingHorizontal,
            this.maxIconSize,
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
            this.pullDownWallPaperDimFade,
            this.pullDownIconFade,
            this.pullDownScaleIn,
            this.toolbarsOrder
        )

    val autoOpenSingleMatch = boolean(
        key = "autoOpenSingleMatch",
        default = true
    )
    val disableAutoLaunchOnSpaceFirstChar = boolean(
        key = "disableAutoLaunchOnSpaceFirstChar",
        default = true
    )

    val showAppIconsInDrawer = boolean(
        key = "showAppIconsInDrawer",
        default = true
    )

    val showAppLabelInDrawer = boolean(
        key = "showAppLabelInDrawer",
        default = true
    )

    val autoShowKeyboardOnDrawer = boolean(
        key = "autoShowKeyboardOnDrawer",
        default = true
    )

    val tapEmptySpaceAction = enum(
        key = "tabEmptySpaceToRaiseKeyboard",
        default = DrawerActions.CLOSE,
        enumClass = DrawerActions::class.java
    )

    val gridSize = int(
        key = "gridSize",
        default = 6,
        allowedRange = 1..15
    )

    val horizontalAlignment = enum(
        key = "horizontalAlignment",
        default = HorizontalAlignment.Start,
        enumClass = HorizontalAlignment::class.java
    )

    val lastWorkspaceUsed = string(
        key = "lastWorkspaceUsed",
        default = "",
    )

    val leftDrawerAction = enum(
        key = "leftDrawerAction",
        default = DrawerActions.defaultLeftDrawerAction,
        enumClass = DrawerActions::class.java
    )

    val rightDrawerAction = enum(
        key = "rightDrawerAction",
        default = DrawerActions.defaultRightDrawerAction,
        enumClass = DrawerActions::class.java
    )

    val leftDrawerWidth = dp(
        key = "leftDrawerWidthDp",
        default = 0.dp,
        allowedRange = 0.dp..50.dp
    )

    val rightDrawerWidth = dp(
        key = "rightDrawerWidthDp",
        default = 0.dp,
        allowedRange = 0.dp..50.dp
    )

    val drawerEnterAction = enum(
        key = "drawerEnterAction",
        default = DrawerActions.defaultEnterAction,
        enumClass = DrawerActions::class.java
    )

    val drawerHomeAction = enum(
        key = "drawerHomeAction",
        default = DrawerActions.defaultHomeAction,
        enumClass = DrawerActions::class.java
    )

    val scrollDownDrawerAction = enum(
        key = "scrollDownDrawerAction",
        default = DrawerActions.defaultScrollDownAction,
        enumClass = DrawerActions::class.java
    )

    val scrollUpDrawerAction = enum(
        key = "scrollUpDrawerAction",
        default = DrawerActions.defaultScrollUpAction,
        enumClass = DrawerActions::class.java
    )

    val backDrawerAction = enum(
        key = "backDrawerAction",
        default = DrawerActions.defaultBackAction,
        enumClass = DrawerActions::class.java
    )

    val iconsShape = shape(
        key = "iconsShape",
        default = IconShape.PlatformDefault
    )


    val iconsSpacingHorizontal = int(
        key = "iconsSpacingHorizontal²",
        default = 8,
        allowedRange = 0..50
    )

    val iconsSpacingVertical = int(
        key = "iconsSpacingVertical",
        default = 8,
        allowedRange = 0..50
    )
    val maxIconSize = int(
        key = "maxIconSize",
        default = 96,
        allowedRange = 0..200
    )

    val useCategory = boolean(
        key = "useCategory",
        default = false
    )

    val categoryGridWidth = int(
        key = "categoryGridWidth",
        default = 3,
        allowedRange = 1..4
    )

    val categoryGridCells = int(
        key = "categoryGridCells",
        default = 3,
        allowedRange = 2..5
    )

    val showCategoryName = boolean(
        key = "showCategoryName",
        default = true
    )

    val showSearchBar = boolean(
        key = "showSearchBar",
        default = true
    )

    val showRecentlyUsedApps = boolean(
        key = "showRecentlyUsedApps",
        default = false
    )

    val recentlyUsedAppsCount = int(
        key = "recentlyUsedAppsCount",
        default = 5,
        allowedRange = 1..20
    )

    val recentlyUsedPackages = stringSet(
        key = "recentlyUsedPackagesSet",
        default = emptySet()
    )

    val pullDownAnimations = boolean(
        key = "pullDownAnimations",
        default = true
    )

    val pullDownWallPaperDimFade = boolean(
        key = "pullDownWallPaperDim",
        default = true
    )

    val pullDownIconFade = boolean(
        key = "pullDownIconFade",
        default = true
    )

    val pullDownScaleIn = boolean(
        key = "pullDownScaleIn",
        default = true
    )

    /**
     * The order of the search bar / recently used in drawer
     */
    val toolbarsOrder = enumList(
        key = "toolbarsOrder2",
        default = DrawerToolbar.defaultDrawerToolbarOrder,
        enumClass = DrawerToolbar::class.java
    )
}