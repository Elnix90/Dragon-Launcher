package org.elnix.dragonlauncher.ui.base.compositionslocals

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore.useCategory
import org.elnix.dragonlauncher.ui.base.asState


data class CategorySettings(
    val useCategory: Boolean,
    val showCategoryName: Boolean,
    val categoryGridCells: Int
)

data class AppItemSettings(
    val showIcons: Boolean,
    val showLabels: Boolean,
    val maxIconSize: Dp,
    val iconSpacingVertical: Dp,
    val iconSpacingHorizontal: Dp,
    val txtColor: Color,
    val gridSize: Int,
    val horizontalAlignment: HorizontalAlignment,
    val categorySettings: CategorySettings
)


@Composable
private fun rememberCategoriesSettings(): CategorySettings {
    val useCategory by useCategory.asState()
    val categoryGridCells by DrawerSettingsStore.categoryGridCells.asState()
    val showCategoryName by DrawerSettingsStore.showCategoryName.asState()

    return remember(
        useCategory,
        categoryGridCells,
        showCategoryName
    ) {
        CategorySettings(
            useCategory = useCategory,
            showCategoryName = showCategoryName,
            categoryGridCells = categoryGridCells
        )
    }
}

@Composable
fun rememberAppItemSettings(): AppItemSettings {

    val showAppIconsInDrawer by DrawerSettingsStore.showAppIconsInDrawer.asState()
    val showAppLabelsInDrawer by DrawerSettingsStore.showAppLabelInDrawer.asState()

    val maxIconSize by DrawerSettingsStore.maxIconSize.asState()

    val iconsSpacingVertical by DrawerSettingsStore.iconsSpacingVertical.asState()
    val iconsSpacingHorizontal by DrawerSettingsStore.iconsSpacingHorizontal.asState()

    val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()

    val gridSize by DrawerSettingsStore.gridSize.asState()
    val txtColor = MaterialTheme.colorScheme.onBackground


    val categorySettings = rememberCategoriesSettings()

    return remember(
        showAppIconsInDrawer,
        showAppLabelsInDrawer,
        maxIconSize,
        iconsSpacingVertical,
        iconsSpacingHorizontal,
        txtColor,
        gridSize,
        horizontalAlignment,
        categorySettings
    ) {
        AppItemSettings(
            showIcons = showAppIconsInDrawer,
            showLabels = showAppLabelsInDrawer,
            maxIconSize = maxIconSize.dp,
            iconSpacingVertical = iconsSpacingVertical.dp,
            iconSpacingHorizontal = iconsSpacingHorizontal.dp,
            txtColor = txtColor,
            gridSize = gridSize,
            horizontalAlignment = horizontalAlignment,
            categorySettings = categorySettings
        )
    }
}


val LocalAppItemSettings = compositionLocalOf<AppItemSettings> { throw IllegalStateException("No AppItemSetting provided") }