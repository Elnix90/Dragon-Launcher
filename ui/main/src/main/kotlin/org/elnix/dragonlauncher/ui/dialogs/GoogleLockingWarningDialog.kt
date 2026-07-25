package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.warning.WarningDialog
import org.elnix.dragonlauncher.ui.warning.WarningManager

@Composable
public fun GoogleLockingWarningDialog() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVersionCode by rememberVersionCode()

    val lastSeenVersionCodeGoogleLockdownWarning by PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.asStateNull()
    val showWarning by WarningManager.showWarningDialog.asState()

    if (lastSeenVersionCodeGoogleLockdownWarning != null && (lastSeenVersionCodeGoogleLockdownWarning!! < currentVersionCode) && showWarning) {
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