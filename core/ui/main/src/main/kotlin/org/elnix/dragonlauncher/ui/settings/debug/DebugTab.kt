@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.debug

import android.content.Intent
import android.provider.Settings
import android.system.Os.kill
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.utils.LifecycleUtils
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.common.utils.detectSystemLauncher
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.InitializationViewModel
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.settings.allStores
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.timer.OverlayReminderService
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun DebugTab(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit,
    initializationViewModel: InitializationViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val systemLauncherPackageName by DebugSettingsStore.systemLauncherPackageName.asState()

    var pendingSystemLauncher by remember { mutableStateOf<String?>(null) }
    var showEditAppOverrides by remember { mutableStateOf(false) }

    val storeResetSectionState = rememberExpandableSection(stringResource(R.string.store_reset))
    val dangerousActionsSectionState = rememberExpandableSection("Dangerous Actions")

    var packageQuery by remember { mutableStateOf("") }
    var packageResult by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        pendingSystemLauncher = ctx.detectSystemLauncher()
    }

    SettingsScaffold(
        title = stringResource(R.string.debug),
        onBack = onBack,
        helpText = "Advanced developer tools and system overrides.",
        onReset = null,
        resetText = null
    ) {
        SettingsSwitchRow(DebugSettingsStore.debugEnabled)

        DragonSettingsGroup(R.string.more) {
            SettingsItem(
                title = stringResource(R.string.logs),
                icon = R.drawable.source_notes
            ) {
                onNavigate(NavigationRoute.Logs)
            }

            SettingsItem(
                title = "Settings debug json",
                icon = R.drawable.settings
            ) {
                onNavigate(NavigationRoute.SettingsJson)
            }
        }

        DragonSettingsGroup(
            title = R.string.ui_flow_and_debug,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DragonButton(
                onClick = { scope.launch { PrivateSettingsStore.lastSeenVersionCodeWhatsNew.reset(ctx) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Show What's New sheet")
            }

            DragonButton(
                onClick = { scope.launch { PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.reset(ctx) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Show Google lockdown warning")
            }

            DragonButton(
                onClick = { scope.launch { PrivateSettingsStore.hasSeenWelcome.reset(ctx) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Show Welcome Screen")
            }

            SettingsSwitchRow(DebugSettingsStore.forceAppLanguageSelector)
            SettingsSwitchRow(PrivateSettingsStore.hideBetaVersionWarning)
            SettingsSwitchRow(PrivateSettingsStore.showSetDefaultLauncherBanner)
            SettingsSwitchRow(DebugSettingsStore.showFps)
            SettingsSwitchRow(DebugSettingsStore.showKillLauncherActionInActionPicker)
            SettingsSwitchRow(UiSettingsStore.doNotRemindMeAgainPinLockWarning)
        }

        DragonSettingsGroup(R.string.debug_infos) {
            SettingsSwitchRow(DebugSettingsStore.debugInfos)
            SettingsSwitchRow(DebugSettingsStore.settingsDebugInfo)
            SettingsSwitchRow(DebugSettingsStore.widgetsDebugInfo)
            SettingsSwitchRow(DebugSettingsStore.workspacesDebugInfo)
        }

        DragonSettingsGroup(R.string.package_search) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = packageQuery,
                    onValueChange = { packageQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search package") },
                    placeholder = { Text("e.g. org.elnix.dragonlauncher.fonts") },
                    singleLine = true,
                    colors = AppObjectsColors.outlinedTextFieldColors()
                )
                DragonButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        packageResult = try {
                            val info = ctx.packageManager.getPackageInfo(packageQuery.trim(), 0)
                            buildString {
                                appendLine("Package: ${info.packageName}")
                                appendLine("Version: ${info.versionName} (${ctx.getVersionCode()}")
                                appendLine("Enabled: ${info.applicationInfo?.enabled ?: "unknown"}")
                                appendLine("Data Dir: ${info.applicationInfo?.dataDir ?: "unknown"}")
                            }
                        } catch (e: Exception) {
                            "Not found or error: $e"
                        }
                    }
                ) {
                    Icon(painter = painterResource(R.drawable.search), contentDescription = null)
                    Text("Search")
                }

                packageResult?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DragonSettingsGroup(
            title = R.string.accessibility_and_system,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SettingsSwitchRow(DebugSettingsStore.useAccessibilityInsteadOfContextToExpandActionPanel,

            )

            SettingsSwitchRow(DebugSettingsStore.autoRaiseDragonOnSystemLauncher)

            DragonButton(
                onClick = { SystemControl.openServiceSettings((ctx)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Accessibility Services")
            }

            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DragonButton(
                        onClick = {
                            pendingSystemLauncher = ctx.detectSystemLauncher()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Detect Launcher")
                    }
                    DragonButton(
                        onClick = {
                            scope.launch {
                                DebugSettingsStore.systemLauncherPackageName.set(
                                    ctx,
                                    pendingSystemLauncher ?: ""
                                )
                            }
                        },
                        enabled = pendingSystemLauncher != null
                    ) {
                        Text("Set Default")
                    }
                }

                pendingSystemLauncher?.let {
                    Text(
                        text = "Detected: $it",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                label = { Text("System launcher package") },
                value = systemLauncherPackageName,
                onValueChange = { newValue ->
                    scope.launch {
                        DebugSettingsStore.systemLauncherPackageName.set(ctx, newValue)
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = AppObjectsColors.outlinedTextFieldColors()
            )
        }

        DragonSettingsGroup(
            title = R.string.test_overlays,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DragonButton(
                onClick = {
                    if (!Settings.canDrawOverlays(ctx)) {
                        ctx.showToast("Overlay permission not granted")
                        return@DragonButton
                    }
                    OverlayReminderService.show(
                        ctx,
                        "TikTok",
                        "15 min",
                        "42 min",
                        "10 min",
                        true,
                        "reminder"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Test: Reminder overlay")
            }

            DragonButton(
                onClick = {
                    if (!Settings.canDrawOverlays(ctx)) {
                        ctx.showToast("Overlay permission not granted")
                        return@DragonButton
                    }
                    OverlayReminderService.show(
                        ctx,
                        "(Fuck) TikTok",
                        "25 min",
                        "58 min",
                        "5 min",
                        true,
                        "time_warning"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Test: Limit overlay")
            }
        }

        DragonSettingsGroup(R.string.risky) {
            ExpandableSection(storeResetSectionState) {
                allStores.entries.forEach { entry ->
                    val settingsStore = entry.value
                    DragonButton(
                        onClick = { scope.launch { settingsStore.resetAll(ctx) } },
                        colors = AppObjectsColors.cancelButtonColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Reset ${settingsStore.dataStoreName.value}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            ExpandableSection(dangerousActionsSectionState) {
                DragonButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { LifecycleUtils.closeApp(ctx as ComponentActivity) }
                ) { Text("Close app (gently)") }

                DragonButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { kill(9, 9) }
                ) { Text("☠\uFE0F Kill Process") }

                DragonButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        ctx.startActivity(
                            Intent(Intent.ACTION_DELETE).apply {
                                data = "package:${ctx.packageName}".toUri()
                            }
                        )
                    }
                ) { Text("☠\uFE0F Uninstall Launcher") }

                DragonButton(
                    onClick = {
                        showEditAppOverrides = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = "Edit ALL app overrides \uD83D\uDE08") }

                DragonButton(
                    onClick = {
                        @Suppress("DIVISION_BY_ZERO")
                        5 / 0
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = "What is 5 / 0? \uD83E\uDD2F") }

                DragonButton(
                    onClick = { initializationViewModel.initialize() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = "Re-initialize points") }

                SettingsSwitchRow(DebugSettingsStore.disableExtensionSignatureCheck)
            }
        }
    }

//    if (showEditAppOverrides) {
//        PointIconEditor(
//            point = dummySwipePoint(),
//            onDismiss = { showEditAppOverrides = false }
//        ) { newIcon ->
//            appsViewModel.applyIconToApps(
//                icon = newIcon
//            )
//            showEditAppOverrides = false
//        }
//    }
}