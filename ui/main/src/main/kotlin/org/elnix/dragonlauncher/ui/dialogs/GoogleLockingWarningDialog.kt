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
import org.elnix.dragonlauncher.ui.warning.GoogleWarningDialog
import org.elnix.dragonlauncher.ui.warning.GoogleWarningManager

@Composable
fun GoogleLockingWarningDialog() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVersionCode by rememberVersionCode()

    val lastSeenVersionCodeGoogleLockdownWarning by PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.asStateNull()
    val showWarning by GoogleWarningManager.showWarningDialog.asState()

    if (lastSeenVersionCodeGoogleLockdownWarning != null && (lastSeenVersionCodeGoogleLockdownWarning!! < currentVersionCode) && showWarning) {
        GoogleWarningDialog(
            onDismissRequest = {
                scope.launch {
                    PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.set(ctx, currentVersionCode)
                }
                GoogleWarningManager.updateWarningDialog(false)
            }
        )
    }
}