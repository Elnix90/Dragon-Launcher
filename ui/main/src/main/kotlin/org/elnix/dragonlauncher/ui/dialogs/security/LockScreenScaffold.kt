package org.elnix.dragonlauncher.ui.dialogs.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.modifiers.conditional


/**
 * A full screen scaffold for lock-screen related screens
 * Currently, [PinPrompt] and [PatternPrompt] uses this function to display the same across the UI
 *
 * The scaffold it treated as **Transparent** by the [org.elnix.dragonlauncher.ui.MainAppUi] thanks to [org.elnix.dragonlauncher.base.navigation.inTransparentScreen], but applies a semi transparent material-themed **background** color to make it a bit more opaque
 * And therefore the user wallpaper is displayed behind it
 */
@Composable
fun LockScreenScaffold(content: @Composable (PaddingValues) -> Unit) {
    val secretUnlockButton by BehaviorSettingsStore.secretUnlockButton.asState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background.alphaMultiplier(0.5f),
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(paddingValues)
            if (secretUnlockButton) {
                SecretUnlockButton(true)
            }
        }
    }
}


/**
 * Secret unlock button that appears in the screen to allow user to remove the lock method when they forgot the password
 * When the user presses it, the lock method is disabled and the stored hash discarded
 * @see [SecurityViewModel.removeLock]
 *
 * @param enabled whether if the button is clickable. If [enabled] is `false`, the button will be visible
 */
@Composable
fun SecretUnlockButton(
    enabled: Boolean,
    securityViewModel: SecurityViewModel = activityViewModel(),
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .offset(y = 120.dp)
            .conditional(!enabled) {
                border(1.dp, Color.White)
                background(Color.Gray.alphaMultiplier(0.5f))
            }
            .clickable(
                indication = null,
                interactionSource = null,
                enabled = enabled
            ) {
                securityViewModel.removeLock()
            }
    )
}
