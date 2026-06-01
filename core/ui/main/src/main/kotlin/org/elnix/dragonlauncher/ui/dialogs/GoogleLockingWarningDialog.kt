package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.warning.WarningDialog
import org.elnix.dragonlauncher.ui.warning.WarningManager

@Composable
fun GoogleLockingWarningDialog() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVersionCode = rememberVersionCode()

    val lastSeenVersionCodeGoogleLockdownWarning by PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.asState()
    val showWarning by WarningManager.showWarningDialog.collectAsStateWithLifecycle()

    if ((lastSeenVersionCodeGoogleLockdownWarning < currentVersionCode) && showWarning) {
        WarningDialog(
            onDismissRequest = {
                scope.launch {
                    PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.set(ctx, currentVersionCode)
                }
                WarningManager.updateWarningDialog(false)
            }
        )
    }
}