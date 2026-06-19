package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.bases.objects.SettingObject
import org.elnix.dragonlauncher.ui.base.asState
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
