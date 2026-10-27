package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.annotations.SettingCall
import io.github.elnix90.core.objects.SettingObject
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.ui.dragon.components.DragonGroupScope
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow

@SettingCall
@Composable
fun DragonGroupScope.Setting(
	setting: SettingObject<Color, String>,
	enabled: Boolean = true,
	onPicked: ((Color?) -> Unit)? = null
) {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	val state by setting.asState()

	ColorPickerRow(
		title = stringResource(setting.title!!),
		description = setting.description?.let { stringResource(it) },
		currentColor = state,
		enabled = enabled,
		defaultColor = setting.default
	) {
		onPicked?.invoke(it)
		scope.launch {
			setting.set(ctx, it)
		}
	}
}
