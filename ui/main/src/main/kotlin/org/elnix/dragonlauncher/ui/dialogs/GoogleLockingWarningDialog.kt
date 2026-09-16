package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.runtime.asMutableState
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.warning.GoogleWarningDialog
import org.elnix.dragonlauncher.ui.warning.GoogleWarningManager

@Composable
fun GoogleLockingWarningDialog() {
    // Show warning in settings
    val showGoogleLockDownWarning by DebugSettingsStore.showGoogleLockDownWarning.asState()
    if (!showGoogleLockDownWarning) return

    // Show warning at each new version
    val versionCode = LocalContext.current.getVersionCode()
    var lastSeenVersionCodeGoogleLockdownWarning by PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.asMutableState()
    if (lastSeenVersionCodeGoogleLockdownWarning >= versionCode) return

    // Show warning when there are still days left
    val showWarning by GoogleWarningManager.showWarningDialog.asState()
    if (!showWarning) return

    GoogleWarningDialog(
        onDismissRequest = {
            lastSeenVersionCodeGoogleLockdownWarning = versionCode
            GoogleWarningManager.updateWarningDialog(false)
        }
    )
}
