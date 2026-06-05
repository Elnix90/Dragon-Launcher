package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow

@Composable
fun GridSizeSlider(apps: List<Application>) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val gridSize by DrawerSettingsStore.gridSize.asState()
    val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()

    var tempGridSize by remember { mutableIntStateOf(gridSize) }

    LaunchedEffect(gridSize) { tempGridSize = gridSize }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SliderWithLabel(
            label = stringResource(R.string.grid_size),
            value = tempGridSize,
            valueRange = 1..10,
            onReset = {
                scope.launch { DrawerSettingsStore.gridSize.reset(ctx) }
            },
            onDragStateChange = {
                scope.launch { DrawerSettingsStore.gridSize.set(ctx, tempGridSize) }
            }
        ) {
            tempGridSize = it
        }

        AnimatedVisibility(
            visible = tempGridSize == 1,
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
                .fillMaxWidth()
                .height(200.dp)
                .clip(DragonShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, DragonShape)
        ) {
            AppGrid(
                apps = apps.shuffled().take(if (tempGridSize == 1) 3 else tempGridSize * 2),
                longPressPopup = false,
                onClick = null
            )
        }
    }
}
