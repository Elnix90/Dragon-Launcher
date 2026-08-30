package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.runtime.asMutableStateNull
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.warning.GoogleWarningDialog
import org.elnix.dragonlauncher.ui.warning.GoogleWarningManager

@Composable
fun GoogleLockingWarningDialog() {
    val versionCode = LocalContext.current.getVersionCode()

    var lastSeenVersionCodeGoogleLockdownWarning by PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.asMutableStateNull()
    val showWarning by GoogleWarningManager.showWarningDialog.asState()

    if (lastSeenVersionCodeGoogleLockdownWarning != null && (lastSeenVersionCodeGoogleLockdownWarning!! < versionCode) && showWarning) {
        GoogleWarningDialog(
            onDismissRequest = {
                lastSeenVersionCodeGoogleLockdownWarning = versionCode
                GoogleWarningManager.updateWarningDialog(false)
            }
        )
    }
}
