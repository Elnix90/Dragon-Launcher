package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.generic.MultiSelectConnectedButtonRow
import io.github.elnix90.shared.ui.dragon.settings.Setting
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.enumsui.select.DrawerAlign.Bottom
import org.elnix.dragonlauncher.base.model.enumsui.select.DrawerAlign.Top
import org.elnix.dragonlauncher.base.model.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dialogs.ToolbarsOrderDialog
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.drawer.AppGrid
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun DrawerAppearanceTab(drawerViewModel: DrawerViewModel = activityViewModel()) {
	val ctx = LocalContext.current
	val drawerSettings = LocalDrawerSettings.current
	val scope = rememberCoroutineScope()

	val apps by drawerViewModel.userApps.collectAsState()

	val gridSize = drawerSettings.gridSize

	val showRecentlyUsed = drawerSettings.showRecentlyUsedApps
	val useCategory = drawerSettings.useCategory

	var showToolbarsOrderDialog by remember { mutableStateOf(false) }
	var showDisabledCategoriesDialog by remember { mutableStateOf(false) }

	SettingsScaffold(
		title = stringResource(NavigationRoute.DrawerAppearance.resId),
		helpText = stringResource(R.string.drawer_appearance_help),
		resetText = null,
		onReset = null,
		bottomContent = {
			DragonSettingsGroup(
				title = R.string.drawer_preview,
				modifier = Modifier.height(200.dp)
			) {
				AppGrid(
					apps = apps.shuffled().take(if (gridSize == 1) 3 else gridSize * 2),
					longPressPopup = false,
					onClick = null,
					modifier = Modifier.dragonSettingGroup()
				)
			}
		}
	) {
		DragonSettingsGroup {
			SettingsItem(
				title = stringResource(R.string.toolbars_order),
				icon = R.drawable._123
			) { showToolbarsOrderDialog = true }

			Setting(DrawerSettingsStore.showAppIconsInDrawer)
			Setting(DrawerSettingsStore.showAppLabelsInDrawer)
			Setting(DrawerSettingsStore.labelTextColor)
			Setting(DrawerSettingsStore.drawerAlign) {
				when (it) {
					Top -> stringResource(R.string.drawer_align_top_desc)
					Bottom -> stringResource(R.string.drawer_align_bottom_desc)
				}
			}
			Setting(DrawerSettingsStore.recentlyInstalledAppsDuration)
			DrawerIconShapePicker()
		}

		DragonSettingsGroup(R.string.recently_used_apps) {
			Setting(DrawerSettingsStore.showRecentlyUsedApps)

			AnimatedVisibility(showRecentlyUsed) {
				Setting(DrawerSettingsStore.recentlyUsedAppsCount)
			}
		}

		DragonSettingsGroup(R.string.category_settings) {
			Setting(DrawerSettingsStore.useCategory)
			Setting(DrawerSettingsStore.showCategoryName, enabled = useCategory)
			Setting(DrawerSettingsStore.categoryCells, enabled = useCategory)
			Setting(DrawerSettingsStore.categoryGridCells, enabled = useCategory)
			Setting(DrawerSettingsStore.categoryColor, enabled = useCategory)
			Setting(DrawerSettingsStore.openableCategories, enabled = useCategory)
			SettingsItem(
				title = stringResource(R.string.disabled_system_categories),
				icon = R.drawable.filter_alt,
				enabled = useCategory
			) { showDisabledCategoriesDialog = true }
		}

		DragonSettingsGroup(R.string.grid_settings) {
			Setting(DrawerSettingsStore.gridSize)
			Setting(DrawerSettingsStore.iconSize) { value ->
				if (value == 0.dp) {
					stringResource(R.string.not_showed)
				} else {
					null
				}
			}
			Setting(DrawerSettingsStore.iconsSpacingHorizontal)
			Setting(DrawerSettingsStore.iconsSpacingVertical)

			val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()

			AnimatedVisibility(gridSize == 1) {
				MultiSelectConnectedButtonRow(
					entries = HorizontalAlignment.entries,
					checked = { horizontalAlignment == it }
				) {
					scope.launch { DrawerSettingsStore.horizontalAlignment.set(ctx, it) }
				}
			}
		}
	}

	if (showToolbarsOrderDialog) {
		ToolbarsOrderDialog { showToolbarsOrderDialog = false }
	}

	if (showDisabledCategoriesDialog) {
		DisabledCategoriesDialog(
			onDismiss = { showDisabledCategoriesDialog = false }
		)
	}
}
