package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.AppCategory
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(
	app: Application,
	drawerViewModel: DrawerViewModel = activityViewModel(),
	existingCustomCategories: List<String>,
	onDismissRequest: () -> Unit
) {
	val ctx = LocalContext.current

	val currentCategory = app.categoryOverride
	var showCreateNew by remember { mutableStateOf(false) }

	fun set(category: String?) {
		drawerViewModel.appOverrideManager.setCustomCategory(app.key, category)
		onDismissRequest()
	}

	DragonModalBottomSheet(
		onDismissRequest = onDismissRequest,
		skipPartiallyExpanded = true
	) {
		DialogTitle(
			text = stringResource(R.string.set_category),
			resetEnabled = currentCategory != null
		) { set(null) }

		Spacer(5.dp)

		Column(
			verticalArrangement = Arrangement.spacedBy(5.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier =
				Modifier
					.heightIn(max = 800.dp)
					.verticalScroll(rememberScrollState())
		) {
			DragonSettingsGroup(R.string.default_category) {
				AppCategory.entries.forEach { category ->
					val displayName = category.name(ctx)

					val selected = currentCategory == category.name || (currentCategory == null && app.category == category)
					val isDefault = app.category == category
					Category(
						name = "$displayName ${if (isDefault) "(${stringResource(R.string.default_text)})" else ""}",
						selected = selected
					) {
						if (isDefault) {
							set(null)
						} else {
							set(category.name)
						}
					}
				}
			}

			Spacer(5.dp)

			DragonSettingsGroup(R.string.custom_category) {
				existingCustomCategories.forEach { categoryName ->
					val selected = currentCategory == categoryName
					Category(
						name = categoryName,
						selected = selected
					) { set(categoryName) }
				}

				Row(
					modifier =
						Modifier
							.dragonSettingGroup {
								clickable { showCreateNew = true }
							}.padding(10.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Icon(
						painter = painterResource(R.drawable.add),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary
					)
					Text(
						text = stringResource(R.string.create_category),
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.primary
					)
				}
			}
		}
	}

	if (showCreateNew) {
		TextEditorDialog(
			title = { stringResource(R.string.category_name) },
			placeHolder = { stringResource(R.string.category_name) },
			defaultText = "",
			initialText = "",
			onDismiss = { showCreateNew = false },
			onValidate = { newCategoryName ->
				set(newCategoryName)
			}
		)
	}
}

@Composable
private fun DragonGroupScope.Category(
	name: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier =
			Modifier
				.dragonSettingGroup(selected = selected) {
					clickable(onClick = onClick)
				}.padding(10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		RadioButton(
			selected = selected,
			onClick = null
		)
		Text(
			text = name,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}
