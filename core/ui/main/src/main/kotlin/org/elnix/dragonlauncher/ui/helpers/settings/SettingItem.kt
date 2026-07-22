package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
public fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    icon: Int,
    trailingIcon: Int? = null,
    onLongClick: (() -> Unit)? = null,
    onExternalClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {

    Row(
        modifier = modifier
            .combinedClickable(
                enabled = enabled,
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.semiTransparentIfDisabled(enabled)
        )

//        CompositionLocalProvider(
//            LocalContentColor provides MaterialTheme.colorScheme.onSurface.semiTransparentIfDisabled(enabled)
//        ) {
            TextWithDescription(
                text = title,
                description = description,
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
//        }

        if (trailingIcon != null) {
            Icon(
                painter = painterResource(trailingIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.semiTransparentIfDisabled(enabled),
                modifier = Modifier
                    .sizeIn(maxHeight = 25.dp)
                    .conditional(onExternalClick != null) {
                        shapedClickable(onClick = onExternalClick!!)
                    }
            )
        }
    }
}

@Composable
public fun RouteItem(
    route: NavigationRoute,
    enabled: Boolean = true,
    onExternalClick: (() -> Unit)? = null,
    onClick: (NavigationRoute) -> Unit
) {
    SettingsItem(
        title = stringResource(route.resId),
        enabled = enabled,
        onExternalClick = onExternalClick,
        icon = route.icon
    ) {
        onClick(route)
    }
}