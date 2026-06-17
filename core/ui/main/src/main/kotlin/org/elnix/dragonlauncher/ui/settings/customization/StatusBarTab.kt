package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayerJson
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.array.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.StatusBarSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsColorPicker
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.EditStatusBar
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import org.elnix.dragonlauncher.ui.statusbar.showStatusBar

@Composable
fun StatusBarTab(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val mainScreenLayers = LocalMainScreenLayers.current
    val showStatusBar by showStatusBar()

    SettingsScaffold(
        title = stringResource(R.string.status_bar),
        onBack = onBack,
        helpText = stringResource(R.string.status_bar_tab_text),
        onReset = {
            scope.launch {
                StatusBarSettingsStore.resetAll(ctx)
                StatusBarJsonSettingsStore.resetAll(ctx)
            }
        }
    ) {
        SwitchRow(
            title = stringResource(R.string.show_status_bar),
            description = stringResource(R.string.show_status_bar_desc),
            state = showStatusBar
        ) {
            scope.launch {
                UiSettingsStore.mainScreenLayers.set(
                    ctx,
                    MainScreenLayerJson.encode(
                        mainScreenLayers.map {
                            if (it is MainScreenLayer.StatusBar) it.copy(enabled = !it.enabled)
                            else it
                        }
                    )
                )
            }
        }

        AnimatedVisibility(showStatusBar) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingsColorPicker(StatusBarSettingsStore.barBackgroundColor)
                SettingsColorPicker(StatusBarSettingsStore.barTextColor,)

                EditStatusBar()

                DragonSettingsGroup(
                    title = R.string.padding,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    SettingsSlider(StatusBarSettingsStore.leftPadding)
                    SettingsSlider(StatusBarSettingsStore.rightPadding)
                    SettingsSlider(StatusBarSettingsStore.topPadding)
                    SettingsSlider(StatusBarSettingsStore.bottomPadding)
                }
            }
        }
    }
    StatusBar(null)
}
