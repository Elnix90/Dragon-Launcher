package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.SocialLink
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.ui.base.components.BoxedIcon
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun DragonGroupScope.SettingsItem(
    title: String,
    icon: Int,
    description: String? = null,
    vararg trailingIcons: SocialLink,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier =
            Modifier
                .dragonSettingGroup(enabled) {
                    clickable(
                        enabled = enabled,
                        onClick = onClick
                    )
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BoxedIcon(icon, enabled)

        TextWithDescription(
            text = title,
            description = description,
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        trailingIcons.forEach {
            BoxedIcon(it.icon) {
                uriHandler.openUri(it.url)
            }
        }
    }
}

@Composable
fun DragonGroupScope.RouteItem(
    route: NavigationRoute,
    vararg trailingIcons: SocialLink,
    enabled: Boolean = true
) {
    val navigator = LocalNavigator.current
    SettingsItem(
        title = stringResource(route.resId),
        enabled = enabled,
        trailingIcons = trailingIcons,
        icon = route.icon
    ) {
        navigator.navigate(route)
    }
}
