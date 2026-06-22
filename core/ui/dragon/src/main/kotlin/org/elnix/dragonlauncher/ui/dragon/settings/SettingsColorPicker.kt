package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import io.github.elnix90.core.objects.SettingObject
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow

@Composable
fun SettingsColorPicker(
    settingObject: SettingObject<Color, String>,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by settingObject.asState()

    ColorPickerRow(
        label = stringResource(settingObject.title!!),
        currentColor = state
    ) {
        scope.launch {
            settingObject.set(ctx, it)
        }
    }
}
