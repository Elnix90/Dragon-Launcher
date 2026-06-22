@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.settings.AllStores
import io.github.elnix90.core.SettingsBackupManager
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dialogs.ExportSettingsDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.helpers.MonospaceScrollableText
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.json.JSONObject

@Composable
fun SettingsDebugTab(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var settingsJson by remember { mutableStateOf<JSONObject?>(null) }

    var selectedStores by remember { mutableStateOf(AllStores) }
    var showStoresDialog by remember { mutableStateOf(false) }

    var forceAllKeys by remember { mutableStateOf(false) }

    fun loadSettings() {
        settingsJson = null
        scope.launch {
            settingsJson = SettingsBackupManager.createJsonToExport(ctx, selectedStores, forceAllKeys)
        }
    }

    val jsonLines by remember(settingsJson) {
        derivedStateOf {
            settingsJson?.toString(2)?.lines().orEmpty()
        }
    }

    LaunchedEffect(Unit, forceAllKeys) {
        loadSettings()
    }

    SettingsScaffold(
        title = "Settings debug json",
        onBack = onBack,
        helpText = "settings json",
        onReset = null,
        resetText = null,
        scrollableContent = false,
        listState = rememberLazyListState(),
        topContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                DragonButton(
                    onClick = { showStoresDialog = true }
                ) {
                    Text("Select visibles stores")
                }

                Spacer(Modifier.weight(1f))

                DragonIconButton(
                    onClick = { forceAllKeys = !forceAllKeys },
                    icon = if (forceAllKeys) R.drawable.add_circle else R.drawable.remove_circle,
                    contentDescription = "Force all keys"
                )

                DragonIconButton(
                    onClick = { settingsJson?.let { ctx.copyToClipboard(it.toString(2)) } },
                    icon = R.drawable.copy,
                    contentDescription = "Copy"
                )

                DragonIconButton(
                    onClick = ::loadSettings,
                    icon = R.drawable.refresh,
                    contentDescription = "Load settings"
                )
            }
        }
    ) {
        MonospaceScrollableText(jsonLines)
    }

    if (showStoresDialog) {
        ExportSettingsDialog(
            onDismiss = { showStoresDialog = false },
            defaultStores = selectedStores,
            availableStores = AllStores
        ) {
            selectedStores = it
            showStoresDialog = false
            loadSettings()
        }
    }
}