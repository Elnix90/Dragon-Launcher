package org.elnix.dragonlauncher.ui.settings.debug

import android.os.Build
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader

@Composable
fun DebugTab(
    navController: NavController,
    onBack: (() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val isDebugModeEnabled by DebugSettingsStore.getDebugEnabled(ctx)
        .collectAsState(initial = false)

    val debugInfos by DebugSettingsStore.getDebugInfos(ctx)
        .collectAsState(initial = false)


    val hasSeenWelcome by PrivateSettingsStore.getHasSeenWelcome(ctx)
        .collectAsState(initial = false)

    val isForceSwitchToggled by DebugSettingsStore.getForceAppLanguageSelector(ctx).collectAsState(initial = false)


    SettingsLazyHeader(
        title = stringResource(R.string.debug),
        onBack = onBack,
        helpText = "Debug, too busy to make a translated explanation",
        onReset = null,
        resetText = null
    ) {

        item{
            SwitchRow(
                state = isDebugModeEnabled,
                text = "Activate Debug Mode",
                defaultValue = true
            ) {
                scope.launch {
                    DebugSettingsStore.setDebugEnabled(ctx, false)
                }
                navController.popBackStack()
            }
        }

        item{
            SwitchRow(
                state = debugInfos,
                text = "Show debug infos",
                defaultValue = false
            ) {
                scope.launch {
                    DebugSettingsStore.setDebugInfos(ctx, it)
                }
            }
        }


        item {
            Button(
                onClick = {
                    scope.launch {
                        PrivateSettingsStore.setHasSeenWelcome(
                            ctx,
                            !hasSeenWelcome
                        )
                    }
                }
            ) {
                Text(
                    text = "Show welcome screen",
                )
            }
        }

        item {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Text(
                    text = "Check this to force the app's language selector instead of the android's one",
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Text(
                    text = "Since you're under android 13, or code name TIRAMISU you can't use the android language selector and you're blocked with the app custom one.",
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

            SwitchRow(
                state = isForceSwitchToggled ,
                text = "Force app language selector",
                enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) { scope.launch { DebugSettingsStore.setForceAppLanguageSelector(ctx, it) } }

        }
    }
}

