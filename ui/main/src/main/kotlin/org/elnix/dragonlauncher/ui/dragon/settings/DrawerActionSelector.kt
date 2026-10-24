package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.ui.dragon.components.DragonGroupScope
import io.github.elnix90.shared.ui.dragon.generic.ActionSelectorRow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DrawerActions

@Composable
fun DragonGroupScope.DrawerActionSelector(
	setting: EnumSettingObject<DrawerActions>,
	icon: Int,
	allowNone: Boolean = false
) {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	val state by setting.asState()

	val actions =
		DrawerActions.entries
			.filter { it != DrawerActions.Disabled }
			.filter { allowNone || it != DrawerActions.None }

	ActionSelectorRow(
		options = actions,
		selected = state,
		label = stringResource(setting.title!!),
		icon = icon,
		optionLabel = { stringResource(it.resId) },
		toggled = state != DrawerActions.Disabled,
		resetEnabled = state != setting.default,
		onReset = {
			scope.launch {
				setting.reset(ctx)
			}
		}
	) {
		scope.launch {
			setting.set(ctx, it)
		}
	}
}
