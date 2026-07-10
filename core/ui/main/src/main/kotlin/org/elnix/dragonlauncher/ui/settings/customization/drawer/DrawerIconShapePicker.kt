package org.elnix.dragonlauncher.ui.settings.customization.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.logging.SHAPES_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@Composable
public fun DrawerIconShapePicker() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val iconShape by DrawerSettingsStore.iconShape.asState()

    var showShapePickerDialog by remember { mutableStateOf(false) }

    ShapeRow(
        selected = iconShape,
        onReset = { scope.launch { DrawerSettingsStore.iconShape.reset(ctx) } }
    ) { showShapePickerDialog = true }

    if (showShapePickerDialog) {
        ShapePickerDialog(
            selected = iconShape,
            onDismiss = { showShapePickerDialog = false }
        ) {
            logD(SHAPES_TAG) { "Picked: $it" }
            scope.launch {
                DrawerSettingsStore.iconShape.set(ctx, it)
            }
        }
    }

}