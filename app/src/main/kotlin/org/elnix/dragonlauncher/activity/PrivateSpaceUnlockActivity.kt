package org.elnix.dragonlauncher.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.theme.DragonLauncherTheme
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.helpers.PrivateSpaceStateDebugScreen
import org.elnix.dragonlauncher.ui.helpers.PrivateSpaceUnlockScreen

class PrivateSpaceUnlockActivity : AppCompatActivity() {

    private val appsViewModel: AppsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DragonLauncherTheme {

                PrivateSpaceUnlockScreen(
                    onCancel = { finish() },
                    onStart = { scope ->
                        scope.launch {
                            appsViewModel.unlockAndReloadPrivateSpace()
                            finish()
                        }
                    }
                )

                val privateSpaceState by appsViewModel.privateSpaceState.collectAsState()

                val privateSpaceDebugInfo by DebugSettingsStore.privateSpaceDebugInfo.asState()
                AnimatedVisibility(privateSpaceDebugInfo) {
                    PrivateSpaceStateDebugScreen(privateSpaceState)
                }
            }
        }
    }
}
