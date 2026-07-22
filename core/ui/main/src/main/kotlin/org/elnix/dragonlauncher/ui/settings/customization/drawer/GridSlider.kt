package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.workspace.AppGrid

@Composable
public fun GridSizeSlider(apps: List<Application>) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val gridSize by DrawerSettingsStore.gridSize.asState()
    val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()

    DragonSettingsGroup(R.string.grid_settings) {
        Setting(DrawerSettingsStore.gridSize)

        AnimatedVisibility(
            visible = gridSize == 1,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            MultiSelectConnectedButtonRow(
                entries = HorizontalAlignment.entries,
                checked = { horizontalAlignment == it }
            ) {
                scope.launch { DrawerSettingsStore.horizontalAlignment.set(ctx, it) }
            }
        }

        Box(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.large)
                .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
        ) {
            AppGrid(
                apps = apps.shuffled().take(if (gridSize == 1) 3 else gridSize * 2),
                longPressPopup = false,
                onClick = null
            )
        }
    }
}
