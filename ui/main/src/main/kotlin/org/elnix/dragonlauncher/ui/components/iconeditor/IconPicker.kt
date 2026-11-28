package org.elnix.dragonlauncher.ui.components.iconeditor

import android.content.pm.PackageManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.theme.AppObjectsColors
import io.github.elnix90.shared.ui.dragon.components.DragonDropDownMenu
import io.github.elnix90.shared.ui.dragon.text.SettingsWithTitle
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.AdaptifiedLegacyIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon.Companion.getProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomIconPackIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomTextIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomThemedIcon
import org.elnix.dragonlauncher.base.model.serializables.DefaultPlaceholderIcon
import org.elnix.dragonlauncher.base.model.serializables.ForceThemedIcon
import org.elnix.dragonlauncher.base.model.serializables.UnmodifiedSystemDefaultIcon
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.icons.CustomIconWithPreview
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.models.IconPickerVM
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.components.ShapedLauncherIcon

@Composable
fun IconPicker(
	application: Application,
	iconsViewModel: IconsViewModel = activityViewModel(),
	contentPadding: PaddingValues = PaddingValues(0.dp),
	onSelect: (CustomIcon?) -> Unit
) {
	val iconSize = 48.dp
	val iconSizePx = iconSize.px
	val focusManager = LocalFocusManager.current
	val ctx = LocalContext.current

	val scope = rememberCoroutineScope()

	val viewModel: IconPickerVM =
		remember(application.key) { iconsViewModel.getIconPickerVM(application) }

	val suggestions by remember { viewModel.getIconSuggestions(iconSizePx.toInt()) }
		.collectAsState(emptyList())

	val defaultIcon by remember {
		viewModel.getDefaultIcon(iconSizePx.toInt())
	}.collectAsState(null)

	var query by remember { mutableStateOf("") }
	var filterIconPack by remember { mutableStateOf<IconPack?>(null) }
	val isSearching by viewModel.isSearchingIcons
	val iconResults by viewModel.iconSearchResults

	var showIconPackFilter by remember { mutableStateOf(false) }
	val installedIconPacks by viewModel.installedIconPacks.collectAsState(null)
	val packsInstalled = installedIconPacks?.isEmpty() == false

	val columns by DrawerSettingsStore.gridSize.asState()

	LazyVerticalGrid(
		modifier =
			Modifier
				.fillMaxSize()
				.heightIn(max = 9999.dp),
		columns = GridCells.Fixed(columns),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = contentPadding
	) {
		if (packsInstalled) {
			item(span = { GridItemSpan(columns) }) {
				TextField(
					value = query,
					onValueChange = {
						query = it
						scope.launch {
							viewModel.searchIcon(query, filterIconPack)
						}
					},
					placeholder = {
						Text(stringResource(R.string.select_icon))
					},
					leadingIcon = {
						Icon(
							painter = painterResource(R.drawable.search),
							contentDescription = null
						)
					},
					colors = AppObjectsColors.outlinedTextFieldColors(removeBorder = true),
					keyboardOptions =
						KeyboardOptions(
							imeAction = ImeAction.Done
						),
					keyboardActions =
						KeyboardActions(
							onDone = { focusManager.clearFocus() }
						),
					shape = CircleShape,
					modifier =
						Modifier
							.width(120.dp)
							.height(50.dp)
				)
			}
		}

		if (query.isEmpty()) {
			if (defaultIcon != null) {
				item(span = { GridItemSpan(columns) }) {
					SettingsWithTitle(stringResource(R.string.icon_picker_default_icon)) {}
				}
				item {
					IconPreview(item = defaultIcon, iconSize = iconSize, onClick = {
						onSelect(null)
					}, application = application)
				}
			}

			if (suggestions.isNotEmpty()) {
				item(span = { GridItemSpan(columns) }) {
					SettingsWithTitle(stringResource(R.string.icon_picker_suggestions)) {}
				}
				items(suggestions) {
					IconPreview(
						it,
						iconSize,
						onClick = { onSelect(it.customIcon) },
						application = application
					)
				}
			}
		} else {
			item(span = { GridItemSpan(columns) }) {
				// I am not touching that thing
				Button(
					onClick = { showIconPackFilter = !showIconPackFilter },
					modifier =
						Modifier
							.wrapContentWidth(align = Alignment.CenterHorizontally)
							.padding(16.dp),
					contentPadding =
						PaddingValues(
							horizontal = 16.dp,
							vertical = 8.dp
						)
				) {
					if (filterIconPack == null) {
						Icon(
							modifier =
								Modifier
									.padding(end = ButtonDefaults.IconSpacing)
									.size(ButtonDefaults.IconSize),
							painter = painterResource(R.drawable.filter_alt),
							contentDescription = null
						)
					} else {
						val icon =
							remember(filterIconPack?.packageName) {
								try {
									filterIconPack?.packageName?.let { pkg ->
										ctx.packageManager.getApplicationIcon(pkg)
									}
								} catch (_: PackageManager.NameNotFoundException) {
									null
								}
							}
						AsyncImage(
							modifier =
								Modifier
									.padding(end = ButtonDefaults.IconSpacing)
									.size(ButtonDefaults.IconSize),
							model = icon,
							contentDescription = null
						)
					}
					DropdownMenuPopup(
						expanded = showIconPackFilter,
						onDismissRequest = { showIconPackFilter = false }
					) {
						DropdownMenuGroup(
							shapes = MenuDefaults.groupShapes()
						) {
							DropdownMenuItem(
								text = { Text(stringResource(id = R.string.icon_picker_filter_all_packs)) },
								onClick = {
									showIconPackFilter = false
									filterIconPack = null
									scope.launch {
										viewModel.searchIcon(query, filterIconPack)
									}
								},
								leadingIcon = {
									if (filterIconPack == null) {
										Icon(painterResource(R.drawable.check), null)
									}
								}
							)
							installedIconPacks?.forEach { iconPack ->
								DropdownMenuItem(
									onClick = {
										showIconPackFilter = false
										filterIconPack = iconPack
										scope.launch {
											viewModel.searchIcon(query, filterIconPack)
										}
									},
									text = { Text(iconPack.name) },
									leadingIcon = {
										if (filterIconPack == iconPack) {
											Icon(painterResource(R.drawable.check), null)
										}
									}
								)
							}
						}
					}
					Text(
						text =
							filterIconPack?.name
								?: stringResource(id = R.string.icon_picker_filter_all_packs),
						modifier = Modifier.animateContentSize()
					)
					Icon(
						painterResource(R.drawable.arrow_drop_down),
						modifier =
							Modifier
								.padding(start = ButtonDefaults.IconSpacing)
								.size(ButtonDefaults.IconSize),
						contentDescription = null
					)
				}
			}

			items(iconResults) {
				IconPreview(
					it,
					iconSize,
					onClick = { onSelect(it.customIcon) },
					application = application
				)
			}

			if (isSearching) {
				item(span = { GridItemSpan(columns) }) {
					Box(
						contentAlignment = Alignment.Center
					) {
						CircularProgressIndicator(
							modifier =
								Modifier
									.padding(12.dp)
									.size(24.dp)
						)
					}
				}
			}
		}
	}
}

@Composable
fun IconPreview(
	item: CustomIconWithPreview?,
	iconSize: Dp,
	onClick: () -> Unit,
	application: Application
) {
	val ctx = LocalContext.current
	var expanded by remember { mutableStateOf(false) }
	val iconPack = item?.customIcon as? CustomIconPackIcon
	val iconPackPackage = iconPack?.iconPackPackage
	val iconPackIcon =
		remember(iconPackPackage) {
			try {
				iconPackPackage?.let { ctx.packageManager.getApplicationIcon(it) }
			} catch (_: PackageManager.NameNotFoundException) {
				null
			}
		}
	val iconPackName =
		remember(iconPackPackage) {
			try {
				iconPackPackage?.let {
					ctx.packageManager
						.getApplicationInfo(it, 0)
						.loadLabel(ctx.packageManager)
						.toString()
				}
			} catch (_: PackageManager.NameNotFoundException) {
				null
			}
		} ?: iconPackPackage

	val iconProperties = item?.customIcon?.getProperties()
	val details =
		buildList {
			add(stringResource(R.string.icon_picker_info_app_package) to application.packageName)
			add(stringResource(R.string.icon_picker_info_app_component) to application.componentName.flattenToShortString())
			when (val customIcon = item?.customIcon) {
				is CustomIconPackIcon -> {
					add(stringResource(R.string.icon_picker_info_icon_name) to (customIcon.drawable ?: customIcon.extras ?: "—"))
					add(stringResource(R.string.icon_picker_info_icon_type) to customIcon.packType)
					add(stringResource(R.string.icon_picker_info_pack_package) to customIcon.iconPackPackage)
					add(stringResource(R.string.icon_picker_info_themed) to customIcon.allowThemed.toString())
					customIcon.extras?.let { add(stringResource(R.string.icon_picker_info_extras) to it) }
				}

				is CustomThemedIcon -> {
					add(stringResource(R.string.icon_picker_info_icon_name) to customIcon.iconPackageName)
					add(stringResource(R.string.icon_picker_info_icon_type) to "Themed")
				}

				is CustomTextIcon -> {
					add(stringResource(R.string.icon_picker_info_icon_name) to customIcon.text)
					add(stringResource(R.string.icon_picker_info_icon_type) to "Text")
				}

				is AdaptifiedLegacyIcon -> {
					add(stringResource(R.string.icon_picker_info_icon_type) to "Adaptified legacy")
					add(stringResource(R.string.icon_picker_info_legacy_scale) to customIcon.fgScale.toString())
					add(stringResource(R.string.icon_picker_info_legacy_background) to customIcon.bgColor.toString())
				}

				is ForceThemedIcon -> {
					add(stringResource(R.string.icon_picker_info_icon_type) to "Forced themed")
				}

				is UnmodifiedSystemDefaultIcon -> {
					add(stringResource(R.string.icon_picker_info_icon_type) to "System default")
				}

				is DefaultPlaceholderIcon -> {
					add(stringResource(R.string.icon_picker_info_icon_type) to "Default placeholder")
				}

				null -> {
					add(stringResource(R.string.icon_picker_info_icon_type) to "Current app icon")
				}
			}

			if (iconProperties?.isNotEmpty == true) {
				val values =
					buildList {
						iconProperties.tint?.let { add("tint=$it") }
						iconProperties.opacity?.let { add("opacity=$it") }
						iconProperties.shape?.let { add("shape=$it") }
						iconProperties.rotationDeg?.let { add("rotation=$it°") }
						iconProperties.scaleX?.let { add("scaleX=$it") }
						iconProperties.scaleY?.let { add("scaleY=$it") }
					}.joinToString()
				add(stringResource(R.string.icon_picker_info_customizations, values) to values)
			}
		}

	Box(
		contentAlignment = Alignment.Center
	) {
		ShapedLauncherIcon(
			size = iconSize,
			icon = { item?.preview },
			modifier = Modifier
				.clip(MaterialTheme.shapes.medium)
				.combinedClickable(
					onClick = onClick,
					onLongClick = { expanded = true }
				)
		)

		DragonDropDownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false }
		) {
			DropdownMenuGroup(
				shapes = MenuDefaults.groupShapes()
			) {
				if (iconPackPackage != null) {
					DropdownMenuItem(
						onClick = { ctx.copyToClipboard(iconPackPackage) },
						shape = MenuDefaults.middleItemShape,
						text = { Text(iconPackName ?: iconPackPackage) },
						leadingIcon = {
							AsyncImage(
								model = iconPackIcon,
								contentDescription = null,
								modifier = Modifier.size(24.dp)
							)
						}
					)
				}

				details.forEachIndexed { index, (text, value) ->
					DropdownMenuItem(
						onClick = { ctx.copyToClipboard(value) },
						shape =
							if (index == details.lastIndex) {
								MenuDefaults.trailingItemShape
							} else {
								MenuDefaults.middleItemShape
							},
						text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
						supportingText = {
							Text(value, style = MaterialTheme.typography.bodySmallEmphasized)
						}
					)
				}
			}
		}
	}
}
