package org.elnix.dragonlauncher.ui.dialogs

import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.ui.actions.ActivityIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.drawer.ListSearch

private data class ActivityItem(
	val activityInfo: LauncherActivityInfo,
	val profile: Profile,
	val packageName: String
) {
	val activityLabel: String get() = activityInfo.label.toString()
	val className: String get() = activityInfo.componentName.className

	fun matchesSearch(q: String, appName: String): Boolean =
		activityLabel.contains(q, ignoreCase = true) ||
			appName.contains(q, ignoreCase = true) ||
			packageName.contains(q, ignoreCase = true) ||
			className.contains(q, ignoreCase = true)
}

/**
 * Dialog that lists every launcher activity of every app (grouped by app), including secondary
 * launcher activities such as the MIUI/HyperOS dialer (see #468), so the user can pick one to
 * add as a swipe action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchActivityPickerDialog(
	profilesViewModel: ProfilesViewModel = activityViewModel(),
	drawerViewModel: DrawerViewModel = activityViewModel(),
	onDismiss: () -> Unit,
	onActivitySelected: (Action.LaunchActivity) -> Unit
) {
	val ctx = LocalContext.current

	var searchQuery by remember { mutableStateOf("") }

	val profiles by profilesViewModel.profiles.collectAsStateWithLifecycle(emptyList())
	val applications by drawerViewModel.allApps.collectAsStateWithLifecycle()

	var activities by remember { mutableStateOf<List<ActivityItem>>(emptyList()) }

	LaunchedEffect(profiles) {
		val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return@LaunchedEffect

		activities =
			withContext(Dispatchers.IO) {
				profiles
					.filterNotNull()
					.mapNotNull { profile ->
						try {
							launcherApps.getActivityList(null, profile.userHandle)?.mapNotNull { info ->
								ActivityItem(
									activityInfo = info,
									profile = profile,
									packageName = info.componentName.packageName
								)
							}
						} catch (_: Exception) {
							null
						}
					}.flatten()
			}.sortedWith(
				compareBy<ActivityItem> { it.packageName.lowercase() }
					.thenBy { it.activityLabel.lowercase() }
			)
	}

	val groupedActivities: Map<String, List<ActivityItem>> =
		remember(activities, applications, searchQuery) {
			activities
				.mapNotNull { item ->
					val appName =
						applications
							.firstOrNull {
								it.packageName == item.packageName && it.profile == item.profile
							}?.label ?: item.activityLabel

					item.takeIf { searchQuery.isBlank() || it.matchesSearch(searchQuery, appName) }?.let { it to appName }
				}.groupBy({ it.second }, { it.first })
				.toSortedMap(String.CASE_INSENSITIVE_ORDER)
		}

	DragonModalBottomSheet(
		onDismissRequest = onDismiss,
		skipPartiallyExpanded = true
	) {
		DialogTitle(stringResource(R.string.launch_activity))
		Spacer(10.dp)

		ListSearch(R.string.search_activities, searchQuery) { searchQuery = it }
		Spacer(10.dp)

		Column(
			modifier =
				Modifier
					.fillMaxWidth()
					.height(800.dp)
					.verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			when {
				activities.isEmpty() || (groupedActivities.isEmpty() && searchQuery.isNotEmpty()) -> {
					val text = stringResource(
						if (groupedActivities.isEmpty() && searchQuery.isNotEmpty()) {
							R.string.no_search_match
						} else {
							R.string.no_activities_found
						}
					)
					Text(
						text = text,
						style = MaterialTheme.typography.bodyLarge
					)

					Text(
						text = "¯\\_(ツ)_/¯",
						style = MaterialTheme.typography.bodyLargeEmphasized
					)
				}

				else -> {
					groupedActivities.forEach { (appName, items) ->
						DragonSettingsGroup(appName) {
							items.forEach { item ->
								ActivityItemRow(
									item = item,
									onClick = {
										onActivitySelected(
											Action.LaunchActivity(
												packageName = item.packageName,
												activityName = item.className,
												profile = item.profile
											)
										)
									}
								)
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun DragonGroupScope.ActivityItemRow(
	item: ActivityItem,
	onClick: () -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier =
			Modifier
				.dragonSettingGroup {
					clickable(onClick = onClick)
				}
	) {
		ActivityIcon(
			action =
				Action.LaunchActivity(
					packageName = item.packageName,
					activityName = item.className,
					profile = item.profile
				),
			size = 36.dp
		)
		TextWithDescription(
			text = item.activityLabel,
			description = item.className
		)
	}
}
