package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingEntry
import io.github.elnix90.shared.ui.base.components.BoxedIcon
import io.github.elnix90.shared.ui.dragon.components.DragonGroupScope
import io.github.elnix90.shared.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.base.model.models.SocialLink
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator

@SettingEntry
@Composable
fun DragonGroupScope.SettingsItem(
	@StringRes title: Int,
	@StringRes description: Int? = null,
	@DrawableRes icon: Int,
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
			text = stringResource(title),
			description = description?.let { stringResource(it) },
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

@SettingEntry
@Composable
fun DragonGroupScope.SplitSettingsItem(
	@StringRes title: Int,
	@StringRes description: Int? = null,
	@DrawableRes icon: Int,
	vararg trailingIcons: SocialLink,
	firstHalfOnClick: () -> Unit,
	firstHalfEnabled: Boolean = true
) {
	val uriHandler = LocalUriHandler.current
	Row(
		modifier = Modifier.dragonSettingGroupSpecialOuter(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
	) {
		Row(
			modifier = with(this@SplitSettingsItem) {
				Modifier.dragonSettingGroupSpecialInner(true) {
					clickable(
						enabled = firstHalfEnabled,
						onClick = firstHalfOnClick
					)
				}
			},
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			BoxedIcon(icon, firstHalfEnabled)

			TextWithDescription(
				text = stringResource(title),
				description = description?.let { stringResource(it) },
				modifier = Modifier.weight(1f),
				enabled = firstHalfEnabled
			)
		}

		Row(
			modifier = with(this@SplitSettingsItem) {
				Modifier.dragonSettingGroupSpecialInner(false)
			},
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			trailingIcons.forEach {
				BoxedIcon(it.icon) {
					uriHandler.openUri(it.url)
				}
			}
		}
	}
}

@Composable
fun DragonGroupScope.RouteItem(
	route: NavigationRoute,
	enabled: Boolean = true
) {
	val navigator = LocalNavigator.current
	SettingsItem(
		title = route.resId,
		enabled = enabled,
		icon = route.icon
	) {
		navigator.navigate(route)
	}
}

@Composable
fun DragonGroupScope.RouteItem(
	route: NavigationRoute,
	vararg trailingIcons: SocialLink,
	enabled: Boolean = true
) {
	val navigator = LocalNavigator.current
	SplitSettingsItem(
		title = route.resId,
		trailingIcons = trailingIcons,
		icon = route.icon,
		firstHalfEnabled = enabled,
		firstHalfOnClick = { navigator.navigate(route) }
	)
}
