package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.helpers.settings.RouteItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@Composable
public fun AppearanceTab(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit
) {
    SettingsScaffold(
        title = stringResource(NavigationRoute.Appearance.resId),
        onBack = onBack,
        helpText = stringResource(R.string.appearance_tab_text),
        resetText = null,
        onReset = null
    ) {
        DragonSettingsGroup(R.string.colors_and_icons) {
            RouteItem(NavigationRoute.Colors) { onNavigate(it) }
            RouteItem(NavigationRoute.IconPack) { onNavigate(it) }
            RouteItem(NavigationRoute.AppDisplay) { onNavigate(it) }
        }

        DragonSettingsGroup(R.string.swipe_related) {
            RouteItem(NavigationRoute.AngleLineEdit) { onNavigate(it) }
            RouteItem(NavigationRoute.HoldToActivateArc) { onNavigate(it) }
            RouteItem(NavigationRoute.MainScreenLayers) { onNavigate(it) }
        }

        DragonSettingsGroup(R.string.other) {
            RouteItem(NavigationRoute.StatusBar) { onNavigate(it) }
            RouteItem(NavigationRoute.Theme, enabled = false) { onNavigate(it) }
            RouteItem(NavigationRoute.Fonts) { onNavigate(it) }
        }
    }
}
