package org.elnix.dragonlauncher.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.SettingsBackupManager
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.BACKUP_TAG
import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.utils.DateUtils
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

/**
 * An interface to use generic function for the presets.
 * ALL MEMBER MUST BE MARKED As [kotlin.io.Serializable]
 *
 * @property name the name of that preset
 */
interface Preset {
	val name: String
}

@Composable
inline fun <reified T : Preset> PresetRow(
	presets: List<T>,
	crossinline get: () -> T,
	crossinline set: (T) -> Unit
) {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()
	val hapticFeedback = LocalHapticFeedback.current

	val importLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.OpenDocument()
	) { uri ->
		if (uri == null) {
			logW(BACKUP_TAG) { "URI is null" }
			ctx.showToast("URI is null")
			return@rememberLauncherForActivityResult
		}

		scope.launch {
			try {
				val jsonString =
					withContext(Dispatchers.IO) {
						ctx.contentResolver
							.openInputStream(uri)
							?.bufferedReader()
							?.use { it.readText() }
					}

				if (jsonString.isNullOrBlank()) {
					logW(BACKUP_TAG) { "Json is empty" }
					ctx.showToast("Json is empty")
					return@launch
				}

				val decoded = json.decodeFromString<T>(jsonString)
				set(decoded)
			} catch (e: Exception) {
				logE(BACKUP_TAG, e) { "Failed to read or decode the preset" }
				ctx.showToast("Failed to read or decode the preset")
			}
		}
	}
	val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
		if (uri == null) {
			return@rememberLauncherForActivityResult
		}

		scope.launch {
			try {
				val encoded = JSONObject(json.encodeToString<T>(get()))
				SettingsBackupManager.writeJson(ctx, uri, encoded)
				ctx.showToast("Preset successfully exported!")
			} catch (e: Exception) {
				logE(BACKUP_TAG, e) { "Failed to export the preset" }
				ctx.showToast("Failed to export the preset")
			}
		}
	}

	DragonSettingsGroup(R.string.presets) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			DragonIconButton(
				icon = R.drawable.share,
				contentDescription = R.string.copy,
				modifier = with(this@DragonSettingsGroup) {
					Modifier
						.width(50.dp)
						.dragonSettingGroup()
				}
			) {
				exportLauncher.launch("${T::class.simpleName}-${DateUtils.nowFormattedDateTime()}.json")
			}
			DragonIconButton(
				icon = R.drawable.add,
				contentDescription = R.string.import_text,
				modifier = with(this@DragonSettingsGroup) {
					Modifier
						.width(50.dp)
						.dragonSettingGroup()
				}
			) {
				importLauncher.launch(
					arrayOf(
						"application/json",
						"text/plain",
						"application/octet-stream",
						"*/*"
					)
				)
			}

			LazyRow(
				modifier = with(this@DragonSettingsGroup) { Modifier.dragonSettingGroup() },
				horizontalArrangement = Arrangement.spacedBy(5.dp)
			) {
				itemsIndexed(presets) { idx, preset ->
					val interactionSource = rememberInteractionSource()
					val isPressed by interactionSource.collectIsPressedAsState()

					var canDelete by remember { mutableStateOf(false) }
					LaunchedEffect(isPressed) {
						if (isPressed && idx > 0) {
							delay(250.milliseconds)
							hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
							canDelete = true
						} else {
							canDelete = false
						}
					}

					val containerColor by animateColorAsState(
						if (canDelete) {
							MaterialTheme.colorScheme.errorContainer
						} else {
							MaterialTheme.colorScheme.primaryContainer
						}
					)

					Button(
						onClick = { set(preset) },
						interactionSource = interactionSource,
						shapes = ButtonDefaults.shapes(),
						colors =
							ButtonDefaults.buttonColors(
								containerColor = containerColor,
								contentColor = contentColorFor(containerColor)
							)
					) {
						Text(preset.name)
					}
				}
			}
		}
	}
}
