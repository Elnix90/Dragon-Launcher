package org.elnix.dragonlauncher.ui.helpers.privatespace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope

@Composable
public fun PrivateSpaceUnlockScreen(
    onCancel: () -> Unit,
    onStart: (CoroutineScope) -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onStart(scope)
    }
    PrivateSpaceLoadingOverlay(onCancel)
}
