package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.util.ColorUtils.definedOrNull
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.base.components.LazyRowWithScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.helpers.IconPackListContent
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun IconPackTab(
    onBack: () -> Unit,
    drawerViewModel: DrawerViewModel = activityViewModel()
) {
    val scope = rememberCoroutineScope()


    val iconPackManager = drawerViewModel.iconPackManager
    val apps by drawerViewModel.userApps.collectAsState(initial = emptyList())
    val selectedPack by iconPackManager.selectedIconPack.collectAsState()
    val packs by drawerViewModel.iconPacksList.collectAsState()

    val iconPackTint by UiSettingsStore.iconPackTint.asStateNull()


    // Used to delay the grid showing up, to prevent lag
    var showPreview by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {

        // Let compose draw at least one frame before showing grid, saves display fps
        withFrameNanos { }
        showPreview = true
    }

    SettingsScaffold(
        title = stringResource(R.string.icon_pack),
        onBack = onBack,
        helpText = stringResource(R.string.icon_pack_help),
        onReset = {
            scope.launch {
                drawerViewModel.clearIconPack()
            }
        },
        topContent = {
            if (showPreview) {
                LazyRowWithScrollIndicator(
                    items = apps,
                    modifier = Modifier.height(70.dp),
                ) { app ->
                    AppIcon(app, 56.dp)
                }
            }
        }
    ) {

        ColorPickerRow(
            label = stringResource(R.string.icon_pack_tint),
            currentColor = iconPackTint ?: Color.Unspecified
        ) {
            scope.launch { drawerViewModel.setIconPackTint(it.definedOrNull()) }
        }

        IconPackListContent(
            packs = packs,
            selectedPackPackage = selectedPack?.packageName,
            showClearOption = true,
            onReloadPacks = {
                drawerViewModel.loadIconPacks()
            },
            onPackClick = { pack ->
                scope.launch {
                    drawerViewModel.selectIconPack(pack)
                }
            },
            onClearClick = {
                scope.launch {
                    drawerViewModel.clearIconPack()
                }
            }
        )
    }
}
