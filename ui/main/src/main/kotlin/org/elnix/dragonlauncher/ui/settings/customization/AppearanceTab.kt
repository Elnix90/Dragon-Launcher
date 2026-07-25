package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.helpers.settings.RouteItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@Composable
fun AppearanceTab() {
    SettingsScaffold(
        title = stringResource(NavigationRoute.Appearance.resId),
        helpText = stringResource(R.string.appearance_tab_text),
        resetText = null,
        onReset = null
    ) {
        DragonSettingsGroup(R.string.colors_and_icons) {
            RouteItem(NavigationRoute.Colors)
            RouteItem(NavigationRoute.IconPack)
            RouteItem(NavigationRoute.AppDisplay)
        }

        DragonSettingsGroup(R.string.swipe_related) {
            RouteItem(NavigationRoute.AngleLineEdit)
            RouteItem(NavigationRoute.HoldToActivateArc)
            RouteItem(NavigationRoute.MainScreenLayers)
        }

        DragonSettingsGroup(R.string.other) {
            RouteItem(NavigationRoute.StatusBar)
            RouteItem(NavigationRoute.Theme, enabled = false)
            RouteItem(NavigationRoute.Fonts)
        }
    }
}
