package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@Composable
fun AppearanceTab(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    SettingsScaffold(
        title = stringResource(R.string.appearance),
        onBack = onBack,
        helpText = stringResource(R.string.appearance_tab_text),
        onReset = {
            scope.launch {
                UiSettingsStore.resetAll(ctx)
            }
        }
    ) {
        DragonSettingsGroup(R.string.colors_and_icons) {
            SettingsItem(
                title = stringResource(R.string.color_selector),
                icon = R.drawable.palette
            ) { onNavigate(NavigationRoute.Colors) }

            SettingsItem(
                title = stringResource(R.string.icon_pack),
                icon = R.drawable.palette
            ) { onNavigate(NavigationRoute.IconPack) }

            SettingsItem(
                title = stringResource(R.string.app_display),
                icon = R.drawable.display_settings
            ) { onNavigate(NavigationRoute.AppDisplay) }

        }

        DragonSettingsGroup(R.string.swipe_related) {
            SettingsItem(
                title = stringResource(R.string.angle_line),
                icon = R.drawable.polyline
            ) { onNavigate(NavigationRoute.AngleLineEdit) }

            SettingsItem(
                title = stringResource(R.string.hold_settings),
                icon = R.drawable.shape_line
            ) { onNavigate(NavigationRoute.HoldToActivateArc) }

            SettingsItem(
                title = stringResource(R.string.main_screen_layers),
                icon = R.drawable.layers
            ) { onNavigate(NavigationRoute.MainScreenLayers) }
        }

        DragonSettingsGroup(R.string.other) {
            SettingsItem(
                title = stringResource(R.string.status_bar),
                icon = R.drawable.android_cell_5
            ) { onNavigate(NavigationRoute.StatusBar) }

            SettingsItem(
                title = stringResource(R.string.theme_selector),
                icon = R.drawable.style
            ) { onNavigate(NavigationRoute.Theme) }

            SettingsItem(
                title = stringResource(R.string.font_selector),
                description = stringResource(R.string.font_selector_desc),
                icon = R.drawable.text_fields_alt
            ) { onNavigate(NavigationRoute.Fonts) }
        }
    }
}
