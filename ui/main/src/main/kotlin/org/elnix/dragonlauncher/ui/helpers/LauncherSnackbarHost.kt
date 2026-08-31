package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.github.elnix90.logging.logLevelChar
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.models.DragonLogViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel

@Composable
fun LauncherSnackbarHost(
    dragonLogViewModel: DragonLogViewModel = activityViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        dragonLogViewModel.alertFlow.collect { alert ->
            if (alert != null) {
                launch {
                    snackbarHostState.showSnackbar(
                        message = "${alert.level.logLevelChar}: ${alert.message}",
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionColor = MaterialTheme.colorScheme.primary,
                actionContentColor = MaterialTheme.colorScheme.onPrimary,
                dismissActionContentColor = MaterialTheme.colorScheme.error
            )
        }
    )
}
