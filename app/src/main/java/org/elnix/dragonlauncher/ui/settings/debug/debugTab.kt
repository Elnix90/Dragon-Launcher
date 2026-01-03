package org.elnix.dragonlauncher.ui.settings.debug

import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.stores.AppsSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.services.SystemControl.activateDeviceAdmin
import org.elnix.dragonlauncher.services.SystemControl.isDeviceAdminActive
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.detectSystemLauncher
import org.elnix.dragonlauncher.utils.models.AppsViewModel

@Composable
fun DebugTab(
    navController: NavController,
    appsViewModel: AppsViewModel,
    onShowWelcome: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val isDebugModeEnabled by DebugSettingsStore.getDebugEnabled(ctx)
        .collectAsState(initial = false)

    val debugInfos by DebugSettingsStore.getDebugInfos(ctx)
        .collectAsState(initial = false)
    val settingsDebugInfos by DebugSettingsStore.getSettingsDebugInfos(ctx)
        .collectAsState(initial = false)
    val widgetsDebugInfos by DebugSettingsStore.getWidgetsDebugInfos(ctx)
        .collectAsState(initial = false)
    val workspaceDebugInfos by DebugSettingsStore.getWorkspacesDebugInfos(ctx)
        .collectAsState(initial = false)

    val useAccessibilityInsteadOfContextToExpandActionPanel by DebugSettingsStore
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


    val systemLauncherPackageName by DebugSettingsStore.getSystemLauncherPackageName(ctx)
        .collectAsState("")
    val autoRaiseDragonOnSystemLauncher by DebugSettingsStore.getAutoRaiseDragonOnSystemLauncher(ctx)
        .collectAsState(false)
    var pendingSystemLauncher by remember { mutableStateOf<String?>(null) }

    val settingsStores = DataStoreName.entries.map { it.store }

    var isAppCacheExpanded by remember { mutableStateOf(false) }

    var appCacheJson by remember { mutableStateOf("") }

    val allApps by appsViewModel.allApps.collectAsState()

    fun reloadAppCacheJson() {
        scope.launch {
            appCacheJson = AppsSettingsStore.getCachedApps(ctx) ?: ""
        }

    }
    LaunchedEffect(Unit) {
        reloadAppCacheJson()
    }

    SettingsLazyHeader(
        title = stringResource(R.string.debug),
        onBack = onBack,
        helpText = "Debug, too busy to make a translated explanation",
        onReset = null,
        resetText = null
    ) {

        item {
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

        item {
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
            SwitchRow(
                state = settingsDebugInfos,
                text = "Show debug infos in settings page",
                defaultValue = false
            ) {
                scope.launch {
                    DebugSettingsStore.setSettingsDebugInfos(ctx, it)
                }
            }
        }

        item {
            SwitchRow(
                state = widgetsDebugInfos,
                text = "Show debug infos in widgets page",
                defaultValue = false
            ) {
                scope.launch {
                    DebugSettingsStore.setWidgetsDebugInfos(ctx, it)
                }
            }
        }

        item {
            SwitchRow(
                state = workspaceDebugInfos,
                text = "Show debug infos in workspace page",
                defaultValue = false
            ) {
                scope.launch {
                    DebugSettingsStore.setWorkspacesDebugInfos(ctx, it)
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
        item {
            SwitchRow(
                state = hasInitialized,
                text = "Has initialized"
            ) {
                scope.launch {
                    PrivateSettingsStore.setHasInitialized(ctx, it)
                }
            }
        }

        item {
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
                state = isForceSwitchToggled,
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
                    DebugSettingsStore.setUseAccessibilityInsteadOfContextToExpandActionPanel(
                        ctx,
                        it
                    )
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
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            reloadAppCacheJson()
                            isAppCacheExpanded = !isAppCacheExpanded
                        }
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAppCacheExpanded) Icons.Default.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(5.dp)
                    )

                    TextDivider("App cache json")
                }


                if (isAppCacheExpanded) {
                    val detailApp = allApps.first()
                    Text(
                        text = """
                            name: ${detailApp.name},
                            packageName: ${detailApp.packageName},
                            enabled: ${detailApp.isEnabled},
                            system: ${detailApp.isSystem},
                            workProfile: ${detailApp.isWorkProfile},
                            launchable: ${detailApp.isLaunchable},
                        """.trimIndent()
                    )
                    Text(
                        text = appCacheJson,
                        fontSize = 10.sp
                    )
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Reset ")
                        withStyle(
                            style = SpanStyle(
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

        item {
            TextButton(
                onClick = { SystemControl.openServiceSettings((ctx)) }
            ) {
                Text("Open Service settings")
            }
            ActivateDeviceAdminButton()

        }


        item {
            SwitchRow(
                state = autoRaiseDragonOnSystemLauncher,
                text = "Auto launch Dragon on system launcher (needs accessibility enabled)",
            ) {
                scope.launch {
                    DebugSettingsStore.setAutoRaiseDragonOnSystemLauncher(ctx, it)
                }
            }
        }


        item {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Button(
                        onClick = {
                            pendingSystemLauncher = detectSystemLauncher(ctx)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Detect System launcher")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                DebugSettingsStore.setSystemLauncherPackageName(
                                    ctx,
                                    pendingSystemLauncher
                                )
                            }
                        },
                        enabled = pendingSystemLauncher != null
                    ) {
                        Text("Set")
                    }
                }

                if (pendingSystemLauncher != null) {
                    Text(
                        buildAnnotatedString {
                            append("Your system launcher: ")
                            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append(pendingSystemLauncher)
                            }
                        }
                    )
                } else {
                    Text("No system launcher detected")
                }
            }
        }
        item {
            OutlinedTextField(
                label = {
                    Text(
                        text = "Your system launcher package name",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                value = systemLauncherPackageName,
                onValueChange = { newValue ->
                    scope.launch {
                        DebugSettingsStore.setSystemLauncherPackageName(ctx, newValue)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = AppObjectsColors.outlinedTextFieldColors()
            )
        }
    }
}


@Composable
fun ActivateDeviceAdminButton() {
    val context = LocalContext.current
    val isActive = remember { mutableStateOf(isDeviceAdminActive(context)) }

    TextButton(
        onClick = {
            Log.d("Compose", "Button clicked - context: ${context.packageName}")
            activateDeviceAdmin(context)
            isActive.value = isDeviceAdminActive(context)
        }
    ) {
        Text(
            if (isActive.value) "Device Admin ✓ Active"
            else "Activate Device Admin"
        )
    }
}
