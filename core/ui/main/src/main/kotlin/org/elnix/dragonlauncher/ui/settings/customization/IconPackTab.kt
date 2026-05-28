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
import org.elnix.dragonlauncher.base.ColorUtils.definedOrNull
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.base.components.LazyRowWithScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.helpers.IconPackListContent
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun IconPackTab(
    onBack: () -> Unit,
    appsViewModel: AppsViewModel = activityViewModel()
) {
    val scope = rememberCoroutineScope()

    val apps by appsViewModel.userApps.collectAsState(initial = emptyList())

    val selectedPack by appsViewModel.selectedIconPack.collectAsState()
    val packs by appsViewModel.iconPacksList.collectAsState()

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
                appsViewModel.clearIconPack()
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
            scope.launch { appsViewModel.setIconPackTint(it.definedOrNull()) }
        }

        IconPackListContent(
            packs = packs,
            selectedPackPackage = selectedPack?.packageName,
            showClearOption = true,
            onReloadPacks = {
                appsViewModel.loadIconPacks()
            },
            onPackClick = { pack ->
                scope.launch {
                    appsViewModel.selectIconPack(pack)
                }
            },
            onClearClick = {
                scope.launch {
                    appsViewModel.clearIconPack()
                }
            }
        )
    }
}
