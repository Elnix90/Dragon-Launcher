package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.messyfolder.openUrl
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState

@Composable
fun GoogleLockingWarningDialog() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVersionCode = rememberVersionCode()

    val lastSeenVersionCodeGoogleLockdownWarning by PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.asState()
    
    if (lastSeenVersionCodeGoogleLockdownWarning < currentVersionCode) {
        GoogleLockingWarning(
            onSolution = {
                ctx.openUrl("https://keepandroidopen.org/")
                scope.launch {
                    PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.set(
                        ctx,
                        currentVersionCode
                    )
                }
            }
        ) {
            scope.launch {
                PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.set(
                    ctx,
                    currentVersionCode
                )
            }
        }
    }
}