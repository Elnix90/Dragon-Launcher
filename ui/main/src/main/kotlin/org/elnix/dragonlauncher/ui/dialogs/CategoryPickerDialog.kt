package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.AppCategory
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerDialog(
    app: Application,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    existingCustomCategories: List<String>,
    onDismissRequest: () -> Unit
) {
    val appOverridesManager = drawerViewModel.appOverrideManager

    var showCreateNew by remember { mutableStateOf(false) }

    val currentCategory = app.categoryOverride

    val systemCategoryNames = AppCategory.entries.map { it.name }
    val allCategoryNames =
        remember(existingCustomCategories, systemCategoryNames) {
            systemCategoryNames + existingCustomCategories.filter { it !in systemCategoryNames }
        }

    fun set(category: String?) {
        appOverridesManager.setCustomCategory(app.key, category)
        onDismissRequest()
    }

    DragonModalBottomSheet(
        onDismissRequest = onDismissRequest,
        true
    ) {
        DialogTitle(
            text = stringResource(R.string.set_category),
            resetEnabled = currentCategory != null
        ) { set(null) }

        Spacer(5.dp)
        DragonSettingsGroup {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Row(
                        modifier =
                            Modifier
                                .dragonSettingGroup(selected = currentCategory == null) {
                                    clickable { set(null) }
                                }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = currentCategory == null,
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.use_default_category),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                items(allCategoryNames) { categoryName ->
                    val selected = currentCategory == categoryName
                    Row(
                        modifier =
                            Modifier
                                .dragonSettingGroup(selected = selected) {
                                    clickable { set(categoryName) }
                                }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null
                        )
                        Text(
                            text = "$categoryName ${if (app.category.name == categoryName) "(${stringResource(R.string.default_text)})" else ""}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(5.dp)

        DragonSettingsGroup(R.string.custom_name) {
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
}
