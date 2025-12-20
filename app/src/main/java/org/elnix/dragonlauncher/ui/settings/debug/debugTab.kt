package org.elnix.dragonlauncher.ui.settings.debug

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors

@Composable
fun DebugTab(
    navController: NavController,
    onShowWelcome: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val isDebugModeEnabled by DebugSettingsStore.getDebugEnabled(ctx)
        .collectAsState(initial = false)

    val debugInfos by DebugSettingsStore.getDebugInfos(ctx)
        .collectAsState(initial = false)


    val useAccessibilityInsteadOfContextToExpandActionPanel by PrivateSettingsStore
        .getUseAccessibilityInsteadOfContextToExpandActionPanel(ctx)
        .collectAsState(initial = false)

    val hasInitialized by PrivateSettingsStore.getHasInitialized(ctx)
        .collectAsState(initial = true)
    val showSetDefaultLauncherBanner by PrivateSettingsStore.getShowSetDefaultLauncherBanner(ctx)
        .collectAsState(initial = true)

    val isForceSwitchToggled by DebugSettingsStore.getForceAppLanguageSelector(ctx)
        .collectAsState(initial = false)

    val doNotRemindMeAgainNotificationsBehavior by PrivateSettingsStore.getShowMethodAsking(ctx)
        .collectAsState(initial = true)


    val settingsStores = DataStoreName.entries.map { it.store }

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

        item { TextDivider("Debug things") }

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
                onClick = { onShowWelcome() },
                colors = AppObjectsColors.buttonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Show welcome screen",
                )
            }
        }


        item {
            Button(
                onClick = {
                    scope.launch { PrivateSettingsStore.setLastSeenVersionCode(ctx, 0) }
                },
                colors = AppObjectsColors.buttonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Show what's new sheet",
                )
            }
        }
        item{
            SwitchRow(
                state = hasInitialized,
                text = "Has initialized"
            ) {
                scope.launch {
                    PrivateSettingsStore.setHasInitialized(ctx, it)
                }
            }
        }

        item{
            SwitchRow(
                state = !showSetDefaultLauncherBanner,
                text = "Hide set default launcher banner",
                defaultValue = true
            ) {
                scope.launch {
                    PrivateSettingsStore.setShowSetDefaultLauncherBanner(ctx, !it)
                }
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

        item {
            SwitchRow(
                state = useAccessibilityInsteadOfContextToExpandActionPanel,
                text = "useAccessibilityInsteadOfContextToExpandActionPanel"
            ) {
                scope.launch {
                    PrivateSettingsStore.setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx, it)
                }
            }
        }

        item {
            SwitchRow(
                state = doNotRemindMeAgainNotificationsBehavior,
                text = "Ask me each times for the notifications / quick settings behavior"
            ) {
                scope.launch {
                    PrivateSettingsStore.setShowMethodAsking(ctx, it)
                }
            }
        }


        item {
            TextDivider(
                text = "Reset",
                lineColor = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error,
            )
        }

        items(settingsStores) { store ->
            OutlinedButton(
                onClick = { scope.launch { store.resetAll(ctx) } },
                colors = AppObjectsColors.cancelButtonColors(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Reset ")
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        ) {
                            append(store.name)
                        }
                        append(" SettingsStore")
                    }
                )
            }
        }

    }
}
