package org.elnix.dragonlauncher.ui.dragon.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.ColorUtils.definedOrNull
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow

@Composable
fun SettingsColorPicker(
    settingObject: BaseSettingObject<Color, String>,
    defaultColor: Color,
    label: String
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val state by settingObject.asStateNull()

    ColorPickerRow(
        label = label,
        currentColor = state.definedOrNull() ?: defaultColor
    ) {
        scope.launch {
            settingObject.set(ctx, it)
        }
    }
}
