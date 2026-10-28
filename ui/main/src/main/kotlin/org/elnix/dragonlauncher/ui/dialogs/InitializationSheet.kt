package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.annotations.SettingHidden
import io.github.elnix90.shared.ui.base.asState
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.dragon.components.DragonButton
import io.github.elnix90.shared.ui.dragon.components.DragonModalBottomSheet
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.components.SliderWithLabel
import io.github.elnix90.shared.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.InitializationViewModel
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.permissionsManager
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@SettingHidden
@Composable
fun InitializationSheet(
	initializationViewModel: InitializationViewModel = activityViewModel(),
	onDismissRequest: () -> Unit
) {
	val ctx = LocalContext.current

	val hasPermission by initializationViewModel.hasUsageStatsPermission.collectAsState()

	DragonModalBottomSheet(
		onDismissRequest = onDismissRequest,
		skipPartiallyExpanded = true
	) {
		DialogTitle(stringResource(R.string.lets_begin))

		Spacer(30.dp)

		DragonSettingsGroup(R.string.auto_import_apps) {
			SettingsItem(
				title = R.string.usage_access,
				icon = R.drawable.analytics,
				enabled = !hasPermission,
				description = if (!hasPermission) R.string.usage_permission_required else R.string.permission_granted
			) { ctx.permissionsManager.requestPermission(PermissionGroup.UsageStat) }

			val howMany by initializationViewModel.howMany.asState()
			SliderWithLabel(
				label = stringResource(R.string.how_many_question),
				description = stringResource(R.string.the_number_of_apps_you_want_to_add),
				value = howMany,
				valueRange = 1..30,
				enabled = hasPermission,
				resetEnabled = true,
				onReset = { initializationViewModel.howMany.value = 30 },
				onChange = { initializationViewModel.howMany.value = it }
			)

			DragonButton(
				onClick = {
					initializationViewModel.initializeWithRecentApps()
					onDismissRequest()
				},
				enabled = hasPermission
			) {
				val text = stringResource(R.string.initialize_apps)
				Icon(
					painter = painterResource(R.drawable.start),
					contentDescription = text
				)
				Spacer(5.dp)
				Text(text)
			}
		}

		Spacer(15.dp)
		DragonSettingsGroup(R.string.or) {
			DragonButton(
				onClick = {
					// Hacky but should work
					initializationViewModel.howMany.value = 30
					initializationViewModel.initializeFresh()
					onDismissRequest()
				}
			) {
				val text = stringResource(R.string.start_fresh)
				Icon(
					painter = painterResource(R.drawable.start),
					contentDescription = text
				)
				Spacer(5.dp)
				Text(text)
			}
		}
	}
}
