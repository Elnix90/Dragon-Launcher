package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.core.objects.SettingObject
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow

@Composable
fun SettingsColorPicker(
    settingObject: SettingObject<Color, String>,
    enabled: Boolean = true,
    onPicked: ((Color?) -> Unit)?=  null
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by settingObject.asState()

    ColorPickerRow(
        title = stringResource(settingObject.title!!),
        description = settingObject.description?.let { stringResource(it) },
        currentColor = state,
        enabled = enabled
    ) {
        onPicked?.invoke(it)
        scope.launch {
            settingObject.set(ctx, it)
        }
    }
}
