package org.elnix.dragonlauncher.ui.settings.fonts

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.annotations.SettingScreen
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.theme.AppObjectsColors
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.base.remember.rememberInteractionSource
import io.github.elnix90.shared.ui.dragon.components.DragonIconButton
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.FONT_PROVIDER
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.fonts.fontNameToFont
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("LocalContextGetResourceValueCall", "WrongConstant")
@SettingScreen(NavigationRoute.Fonts::class)
@Composable
fun FontTab() {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	val globalFontName by UiSettingsStore.globalFont.asState()

	var refreshTrigger by remember { mutableIntStateOf(0) }

	val selectedFontsToDelete = remember { mutableStateListOf<String>() }
	var isDeleteMode by remember { mutableStateOf(false) }

	val availableFonts =
		remember(refreshTrigger) {
			val base = listOf("Default", "Serif", "SansSerif", "Monospace", "Cursive")
			val extFonts =
				try {
					val extDir = File(ctx.getExternalFilesDir(null), "fonts")
					if (extDir.exists()) {
						extDir
							.listFiles { file -> file.extension == "ttf" || file.extension == "otf" }
							?.map { it.nameWithoutExtension }
							?.sorted() ?: emptyList()
					} else {
						emptyList()
					}
				} catch (_: Exception) {
					emptyList()
				}
			base + extFonts
		}

	val filePickerLauncher =
		rememberLauncherForActivityResult(
			contract = ActivityResultContracts.OpenDocument(),
			onResult = { uri: Uri? ->
				uri?.let {
					scope.launch {
						try {
							val fileName =
								ctx.contentResolver.query(it, null, null, null, null)?.use { cursor ->
									val nameIndex =
										cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
									cursor.moveToFirst()
									cursor.getString(nameIndex)
								} ?: "custom_font_${System.currentTimeMillis()}.ttf"
							val destDir = File(ctx.getExternalFilesDir(null), "fonts")
							if (!destDir.exists()) destDir.mkdirs()
							val destFile = File(destDir, fileName)
							ctx.contentResolver.openInputStream(it)?.use { input ->
								FileOutputStream(destFile).use { output -> input.copyTo(output) }
							}
							val fontName = fileName.substringBeforeLast(".")
							UiSettingsStore.globalFont.set(ctx, fontName)
							refreshTrigger = (refreshTrigger + 1) % 1000
							ctx.showToast("Font $fontName added!")
						} catch (e: Exception) {
							logE(FONT_PROVIDER, e) { "Error importing font" }
							ctx.showToast("Error importing font")
						}
					}
				}
			}
		)

	SettingsScaffold(
		title = stringResource(R.string.font_selector),
		helpText = stringResource(R.string.font_manage_help),
		onReset = null,
		resetText = null,
		lazyContent = {
			item {
				Column(
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp)
				) {
					val interactionSources = List(4) { rememberInteractionSource() }

					AnimatedContent(isDeleteMode) {
						ButtonGroup(
							overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.spacedBy(8.dp)
						) {
							if (!it) {
								customItem(
									buttonGroupContent = {
										OutlinedButton(
											onClick = {
												filePickerLauncher.launch(
													arrayOf(
														"font/ttf",
														"font/otf",
														"application/x-font-ttf",
														"application/x-font-otf",
														"application/octet-stream"
													)
												)
											},
											interactionSource = interactionSources[0],
											modifier =
												Modifier
													.weight(3f)
													.animateWidth(interactionSources[0]),
											shapes = ButtonDefaults.shapes()
										) {
											Icon(
												painter = painterResource(R.drawable.add),
												contentDescription = null,
												modifier = Modifier.size(16.dp)
											)
											Spacer(6.dp)
											Text(
												stringResource(R.string.font_import),
												fontSize = 12.sp,
												maxLines = 1
											)
										}
									},
									menuContent = {}
								)

								customItem(
									buttonGroupContent = {
										DragonIconButton(
											icon = R.drawable.delete_forever,
											contentDescription = R.string.delete,
											interactionSource = interactionSources[1],
											modifier =
												Modifier
													.weight(1f)
													.animateWidth(interactionSources[1])
										) { isDeleteMode = true }
									},
									menuContent = {}
								)
							} else {
								customItem(
									buttonGroupContent = {
										Button(
											onClick = {
												if (selectedFontsToDelete.isNotEmpty()) {
													scope.launch {
														try {
															val extDir =
																File(ctx.getExternalFilesDir(null), "fonts")
															selectedFontsToDelete.forEach { font ->
																File(extDir, "$font.ttf").delete()
																File(extDir, "$font.otf").delete()
															}
															selectedFontsToDelete.clear()
															isDeleteMode = false
															refreshTrigger = (refreshTrigger + 1) % 1000
															ctx.showToast("Selected fonts deleted")
														} catch (e: Exception) {
															logE(
																FONT_PROVIDER,
																e
															) { "Error deleting fonts" }
															ctx.showToast("Error deleting fonts")
														}
													}
												} else {
													isDeleteMode = false
												}
											},
											interactionSource = interactionSources[2],
											modifier =
												Modifier
													.weight(4f)
													.animateWidth(interactionSources[2]),
											contentPadding =
												PaddingValues(
													horizontal = 8.dp,
													vertical = 8.dp
												),
											shapes = ButtonDefaults.shapes(),
											colors = AppObjectsColors.cancelButtonColors()
										) {
											Icon(
												painter = painterResource(R.drawable.check),
												contentDescription = null
											)
											Spacer(6.dp)
											Text(
												stringResource(
													R.string.font_confirm_delete,
													selectedFontsToDelete.size
												),
												fontSize = 12.sp,
												maxLines = 1
											)
										}
									},
									menuContent = { }
								)

								customItem(
									buttonGroupContent = {
										IconButton(
											onClick = {
												isDeleteMode = false
												selectedFontsToDelete.clear()
											},
											interactionSource = interactionSources[3],
											modifier =
												Modifier
													.weight(1f)
													.animateWidth(interactionSources[3]),
											colors = AppObjectsColors.cancelIconButtonColors(),
											shapes = IconButtonDefaults.shapes()
										) {
											Icon(
												painter = painterResource(R.drawable.cancel),
												contentDescription = null
											)
										}
									},
									menuContent = { }
								)
							}
						}
					}
				}
			}

			item {
				DragonSettingsGroup(R.string.font_installed_fonts) {
					if (availableFonts.isNotEmpty()) {
						availableFonts.forEach { font ->
							val isDeletable =
								font !in
									listOf(
										"Default",
										"Serif",
										"SansSerif",
										"Monospace",
										"Cursive"
									)
							FontRow(
								font = font,
								selected =
									if (isDeleteMode) {
										selectedFontsToDelete.contains(
											font
										)
									} else {
										globalFontName == font
									},
								showCheckbox = isDeleteMode && isDeletable
							) {
								if (isDeleteMode) {
									if (isDeletable) {
										if (selectedFontsToDelete.contains(font)) {
											selectedFontsToDelete.remove(
												font
											)
										} else {
											selectedFontsToDelete.add(font)
										}
									}
								} else {
									scope.launch {
										UiSettingsStore.globalFont.set(ctx, font)
									}
								}
							}
						}
					}
				}
			}
		}
	)
}

@Composable
private fun FontRow(
	font: String,
	selected: Boolean,
	showCheckbox: Boolean = false,
	onClick: () -> Unit
) {
	val ctx = LocalContext.current

	val animatedBgColor by animateColorAsState(
		if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
	)

	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.clip(MaterialTheme.shapes.extraSmall)
				.background(animatedBgColor)
				.clickable { onClick() }
				.padding(20.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (showCheckbox) {
			Checkbox(
				checked = selected,
				onCheckedChange = null,
				colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
			)
		} else {
			RadioButton(
				selected = selected,
				onClick = null,
				colors = AppObjectsColors.radioButtonColors()
			)
		}
		Spacer(12.dp)
		val fontFamily =
			try {
				fontNameToFont(font, ctx)
			} catch (_: Exception) {
				FontFamily.Default
			}
		Text(
			text = font,
			fontFamily = fontFamily,
			color = MaterialTheme.colorScheme.onSurface,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
