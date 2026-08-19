package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.models.IconSettings
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel

data class DrawerSettings(
    val iconSize: Dp,
    val useCategory: Boolean,
    val gridSize: Int,
    val categoryGridCells: Int,
    val showAppIconsInDrawer: Boolean,
    val showAppLabelsInDrawer: Boolean,
    val labelTextColor: Color,
    val iconsSpacingVertical: Dp,
    val iconsSpacingHorizontal: Dp,
    val horizontalAlignment: HorizontalAlignment,
    val iconShape: IconShape,
    val iconSettings: IconSettings,
    val autoAskToUnlockProfile: Boolean,
    val autoOpenSingleMatch: Boolean,
    val disableAutoLaunchWhenFirstCharIs: String,
    val tapEmptySpaceAction: DrawerActions,
    val drawerEnterAction: DrawerActions,
    val drawerBackAction: DrawerActions,
    val drawerHomeAction: DrawerActions,
    val drawerScrollDownAction: DrawerActions,
    val drawerScrollUpAction: DrawerActions,
    val showSearchBar: Boolean,
    val showRecentlyUsedApps: Boolean,
    val recentlyUsedAppsCount: Int,
    val autoShowKeyboard: Boolean,
    val toolbarsOrder: List<DrawerToolbar>
)

val LocalDrawerSettings: ProvidableCompositionLocal<DrawerSettings> = compositionLocalOf { error("No DrawerSettings provided") }

@Composable
fun ProvideDrawerSettings(
    iconsViewModel: IconsViewModel = activityViewModel(),
    content: @Composable () -> Unit
) {
    val iconSize by DrawerSettingsStore.iconSize.asState()
    val useCategory by DrawerSettingsStore.useCategory.asState()
    val gridSize by DrawerSettingsStore.gridSize.asState()
    val categoryGridCells by DrawerSettingsStore.categoryGridCells.asState()
    val showAppIconsInDrawer by DrawerSettingsStore.showAppIconsInDrawer.asState()
    val showAppLabelsInDrawer by DrawerSettingsStore.showAppLabelsInDrawer.asState()
    val labelTextColor by DrawerSettingsStore.labelTextColor.asState()
    val iconsSpacingVertical by DrawerSettingsStore.iconsSpacingVertical.asState()
    val iconsSpacingHorizontal by DrawerSettingsStore.iconsSpacingHorizontal.asState()
    val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()
    val iconShape by DrawerSettingsStore.iconShape.asState()
    val iconSettings by iconsViewModel.iconSettings.collectAsState()
    val autoAskToUnlockProfile by DrawerSettingsStore.autoAskToUnlockProfile.asState()
    val autoOpenSingleMatch by DrawerSettingsStore.autoOpenSingleMatch.asState()
    val disableAutoLaunchWhenFirstCharIs by DrawerSettingsStore.disableAutoLaunchWhenFirstCharIs.asState()
    val tapEmptySpaceAction by DrawerSettingsStore.tapEmptySpaceAction.asState()
    val drawerEnterAction by DrawerSettingsStore.drawerEnterAction.asState()
    val drawerBackAction by DrawerSettingsStore.drawerBackAction.asState()
    val drawerHomeAction by DrawerSettingsStore.drawerHomeAction.asState()
    val drawerScrollDownAction by DrawerSettingsStore.drawerScrollDownAction.asState()
    val drawerScrollUpAction by DrawerSettingsStore.drawerScrollUpAction.asState()
    val showSearchBar by DrawerSettingsStore.showSearchBar.asState()
    val showRecentlyUsedApps by DrawerSettingsStore.showRecentlyUsedApps.asState()
    val recentlyUsedAppsCount by DrawerSettingsStore.recentlyUsedAppsCount.asState()
    val autoShowKeyboard by DrawerSettingsStore.autoShowKeyboardOnDrawer.asState()
    val toolbarsOrder by DrawerSettingsStore.toolbarsOrder.asState()


    CompositionLocalProvider(
        LocalDrawerSettings provides DrawerSettings(
            iconSize = iconSize,
            useCategory = useCategory,
            gridSize = gridSize,
            categoryGridCells = categoryGridCells,
            showAppIconsInDrawer = showAppIconsInDrawer,
            showAppLabelsInDrawer = showAppLabelsInDrawer,
            labelTextColor = labelTextColor,
            iconsSpacingVertical = iconsSpacingVertical,
            iconsSpacingHorizontal = iconsSpacingHorizontal,
            horizontalAlignment = horizontalAlignment,
            iconShape = iconShape,
            iconSettings = iconSettings,
            autoAskToUnlockProfile = autoAskToUnlockProfile,
            autoOpenSingleMatch = autoOpenSingleMatch,
            disableAutoLaunchWhenFirstCharIs = disableAutoLaunchWhenFirstCharIs,
            tapEmptySpaceAction = tapEmptySpaceAction,
            drawerEnterAction = drawerEnterAction,
            drawerBackAction = drawerBackAction,
            drawerHomeAction = drawerHomeAction,
            drawerScrollDownAction = drawerScrollDownAction,
            drawerScrollUpAction = drawerScrollUpAction,
            showSearchBar = showSearchBar,
            showRecentlyUsedApps = showRecentlyUsedApps,
            recentlyUsedAppsCount = recentlyUsedAppsCount,
            autoShowKeyboard = autoShowKeyboard,
            toolbarsOrder = toolbarsOrder
        ),
        content = content
    )
}