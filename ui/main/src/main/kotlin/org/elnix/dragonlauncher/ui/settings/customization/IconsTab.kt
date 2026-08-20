package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.settings.stores.map.IconsSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.LazyRowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.IconPackListContent
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.settings.customization.drawer.DrawerIconShapePicker

@Composable
fun IconsTab(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    iconsViewModel: IconsViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()

    val apps by drawerViewModel.userApps.collectAsState(initial = emptyList())
    val packs by drawerViewModel.getInstalledIconPacks().collectAsState(emptyList())

    val iconSettings by iconsViewModel.iconSettings.collectAsState()
    val selectedPack = iconSettings.iconPack

    SettingsScaffold(
        title = stringResource(NavigationRoute.Icons.resId),
        onBack =  {
            iconsViewModel.reloadAllPointsIcons()
            navigator.onBack()
        },
        helpText = stringResource(R.string.icon_pack_help),
        resetText = stringResource(R.string.reset_icon_packs_tab),
        onReset = {
            scope.launch {
                IconsSettingsStore.resetAll(ctx)
            }
        },
        topContent = {
            LazyRowWithScrollIndicator(
                items = apps,
                modifier = Modifier.height(70.dp),
            ) { app ->
                AppIcon(app, size = 56.dp)
            }
        }
    ) {

        // because of the icons in top content
        Spacer(30.dp)

        DragonSettingsGroup(R.string.colors_and_icons) {
            Setting(IconsSettingsStore.useIconTint)

            val useIconTint by IconsSettingsStore.useIconTint.asState()
            Setting(IconsSettingsStore.onlyTintIconPack, enabled = useIconTint) {
                iconsViewModel.reinstallAllIconPacks()
            }
            Setting(IconsSettingsStore.iconsTint, enabled = useIconTint) {
                iconsViewModel.reinstallAllIconPacks()
            }

            Setting(IconsSettingsStore.renderForeground)
            Setting(IconsSettingsStore.renderBackground)

            Setting(IconsSettingsStore.themedIcons)

            val themedIcons by IconsSettingsStore.themedIcons.asState()
            Setting(IconsSettingsStore.forceThemed, enabled = themedIcons)

            Setting(IconsSettingsStore.adaptify)

            DrawerIconShapePicker()
        }

        IconPackListContent(
            packs = packs,
            selectedPackPackage = selectedPack,
            showClearOption = true,
            onPackClick = { pack ->
                scope.launch {
                    IconsSettingsStore.selectedIconPack.set(ctx, pack.packageName)
                }
            },
            onClearClick = {
                scope.launch {
                    IconsSettingsStore.selectedIconPack.reset(ctx)
                }
            }
        )
    }
}
