package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DrawerActions.Companion.notDisabled
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DrawerActions.Companion.notNone
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.modifiers.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.settings.DrawerActionSelector
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun DrawerActionsTab() {
	val ctx = LocalContext.current
	val drawerSettings = LocalDrawerSettings.current
	val scope = rememberCoroutineScope()

	val leftDrawerAction = drawerSettings.leftDrawerAction
	val rightDrawerAction = drawerSettings.rightDrawerAction
	val leftDrawerWidth = drawerSettings.leftDrawerWidth
	val rightDrawerWidth = drawerSettings.rightDrawerWidth

	var leftWidth by remember { mutableStateOf(leftDrawerWidth) }
	var rightWidth by remember { mutableStateOf(rightDrawerWidth) }

	LaunchedEffect(leftDrawerWidth, rightDrawerWidth) {
		leftWidth = leftDrawerWidth
		rightWidth = rightDrawerWidth
	}

	SettingsScaffold(
		title = stringResource(R.string.drawer_actions),
		helpText = stringResource(R.string.drawer_actions_help),
		resetText = null,
		onReset = null
	) {
		DragonSettingsGroup {
			DrawerActionSelector(DrawerSettingsStore.drawerScrollUpAction, R.drawable.keyboard_arrow_up)
			DrawerActionSelector(DrawerSettingsStore.drawerScrollDownAction, R.drawable.arrow_down)
			DrawerActionSelector(DrawerSettingsStore.tapEmptySpaceAction, R.drawable.circle)
			DrawerActionSelector(DrawerSettingsStore.drawerBackAction, R.drawable.back)
			DrawerActionSelector(DrawerSettingsStore.drawerEnterAction, R.drawable.start)
			DrawerActionSelector(DrawerSettingsStore.drawerHomeAction, R.drawable.home)
			DrawerActionSelector(DrawerSettingsStore.drawerClickSearchIconAction, R.drawable.search)
		}

		DragonSettingsGroup(R.string.width_actions_settings) {
			DrawerActionSelector(DrawerSettingsStore.leftDrawerAction, R.drawable.keyboard_arrow_left, allowNone = true)
			DrawerActionSelector(DrawerSettingsStore.rightDrawerAction, R.drawable.arrow_right, allowNone = true)
		}

		DragonSettingsGroup(
			R.string.drawer_actions_width,
			trailingIcon = {
				ResetIcon(
					leftWidth != DrawerSettingsStore.leftDrawerWidth.default || rightWidth != DrawerSettingsStore.rightDrawerWidth.default
				) {
					scope.launch {
						DrawerSettingsStore.leftDrawerWidth.reset(ctx)
						DrawerSettingsStore.rightDrawerWidth.reset(ctx)
					}
				}
			}
		) {
			Box(
				modifier =
					Modifier
						.height(50.dp)
						.dragonSettingGroup(leftDrawerAction.notDisabled || rightDrawerAction.notDisabled)
			) {
				if (leftDrawerAction.notDisabled) {
					Row(
						modifier = Modifier.align(Alignment.CenterStart)
					) {
						Box(
							modifier =
								Modifier
									.fillMaxHeight()
									.width(leftWidth)
									.background(MaterialTheme.colorScheme.primary)
									.semiTransparentIfDisabled(leftDrawerAction.notNone),
							contentAlignment = Alignment.Center
						) {
							if (leftDrawerAction.notNone) {
								Icon(
									painter = painterResource(leftDrawerAction.iconEnabled),
									contentDescription = stringResource(R.string.left_drawer_action),
									tint = MaterialTheme.colorScheme.outline
								)
							}
						}
						CustomDragHandle()
					}
				}

				if (rightDrawerAction.notDisabled) {
					Row(
						modifier = Modifier.align(Alignment.CenterEnd)
					) {
						CustomDragHandle()
						Box(
							modifier =
								Modifier
									.fillMaxHeight()
									.width(rightWidth)
									.background(MaterialTheme.colorScheme.primary)
									.semiTransparentIfDisabled(rightDrawerAction.notNone),
							contentAlignment = Alignment.Center
						) {
							if (rightDrawerAction.notNone) {
								Icon(
									painter = painterResource(rightDrawerAction.iconEnabled),
									contentDescription = stringResource(R.string.right_drawer_action),
									tint = MaterialTheme.colorScheme.outline
								)
							}
						}
					}
				}
			}

			SliderWithLabel(
				label = stringResource(DrawerSettingsStore.leftDrawerWidth.title!!),
				value = leftWidth,
				valueRange = DrawerSettingsStore.leftDrawerWidth.allowedRange,
				enabled = leftDrawerAction.notDisabled,
				resetEnabled = leftWidth != DrawerSettingsStore.leftDrawerWidth.default,
				onReset = {
					leftWidth = leftDrawerWidth
					scope.launch {
						DrawerSettingsStore.leftDrawerWidth.reset(ctx)
					}
				},
				onDragStateChange = { isDragging ->
					if (!isDragging) {
						scope.launch {
							DrawerSettingsStore.leftDrawerWidth.set(ctx, leftWidth)
						}
					}
				}
			) { leftWidth = it }

			SliderWithLabel(
				label = stringResource(R.string.right_drawer_width),
				value = rightWidth,
				valueRange = DrawerSettingsStore.rightDrawerWidth.allowedRange,
				enabled = rightDrawerAction.notDisabled,
				resetEnabled = rightWidth != DrawerSettingsStore.rightDrawerWidth.default,
				onReset = {
					rightWidth = rightDrawerWidth
					scope.launch {
						DrawerSettingsStore.rightDrawerWidth.reset(ctx)
					}
				},
				onDragStateChange = { isDragging ->
					if (!isDragging) {
						scope.launch {
							DrawerSettingsStore.rightDrawerWidth.set(ctx, rightWidth)
						}
					}
				}
			) { rightWidth = it }
		}
	}
}

/**
 * Renamed in with "Custom" because otherwise my IDE kept auto-importing the lib DragHandle and this was annoying
 */
@Composable
private fun CustomDragHandle() {
	Box(
		modifier =
			Modifier
				.fillMaxHeight()
				.width(6.dp)
				.background(MaterialTheme.colorScheme.outline)
	)
}
