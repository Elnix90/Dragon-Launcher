package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.MainScreenLayer
import org.elnix.dragonlauncher.common.serializables.MainScreenLayerJson
import org.elnix.dragonlauncher.settings.stores.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.StatusBarSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
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
    val showStatusBar = showStatusBar()

    val paddingsSectionState = rememberExpandableSection(stringResource(R.string.padding), mode = ExpandableSectionMode.Expandable)


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
            state = showStatusBar()
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

                SettingsColorPicker(
                    settingObject = StatusBarSettingsStore.barBackgroundColor,
                    label = stringResource(R.string.status_bar_background),
                    defaultColor = Color.Transparent
                )

                SettingsColorPicker(
                    settingObject = StatusBarSettingsStore.barTextColor,
                    label = stringResource(R.string.status_bar_text_color),
                    defaultColor = MaterialTheme.colorScheme.primary
                )

                EditStatusBar()

                DragonSettingsGroup(
                    title = R.string.padding,
                    contentPadding = PaddingValues(12.dp)) {
                    SettingsSlider(
                        setting = StatusBarSettingsStore.leftPadding,
                        title = stringResource(R.string.left_padding),
                        valueRange = 0..200,
                    )

                    SettingsSlider(
                        setting = StatusBarSettingsStore.rightPadding,
                        title = stringResource(R.string.right_padding),
                        valueRange = 0..200,
                    )
                    SettingsSlider(
                        setting = StatusBarSettingsStore.topPadding,
                        title = stringResource(R.string.top_padding),
                        valueRange = 0..200,
                    )
                    SettingsSlider(
                        setting = StatusBarSettingsStore.bottomPadding,
                        title = stringResource(R.string.bottom_padding),
                        valueRange = 0..200,
                    )
                }
            }
        }
    }
    StatusBar(null)
}
