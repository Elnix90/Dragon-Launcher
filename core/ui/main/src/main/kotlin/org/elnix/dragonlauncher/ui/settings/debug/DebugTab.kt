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
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.dummySwipePoint
import org.elnix.dragonlauncher.common.utils.LifecycleUtils
import org.elnix.dragonlauncher.common.utils.PermissionsUtils.detectSystemLauncher
import org.elnix.dragonlauncher.common.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.models.InitializationViewModel
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.settings.allStores
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dialogs.PointIconEditor
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.wellbeing.OverlayReminderService

@Composable
fun DebugTab(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit,
    appsViewModel: AppsViewModel = activityViewModel(),
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
        pendingSystemLauncher = detectSystemLauncher(ctx)
    }

    SettingsScaffold(
        title = stringResource(R.string.debug),
        onBack = onBack,
        helpText = "Advanced developer tools and system overrides.",
        onReset = null,
        resetText = null
    ) {

        SettingsSwitchRow(
            setting = DebugSettingsStore.debugEnabled,
            title = stringResource(R.string.activate_debug_mode),
            description = stringResource(R.string.activate_debug_mode_desc)
        ) {
            scope.launch { DebugSettingsStore.debugEnabled.set(ctx, it) }
        }

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

            SettingsSwitchRow(
                setting = DebugSettingsStore.forceAppLanguageSelector,
                title = "Force app language selector",
                description = "Don't use the android language selector when available, always uses the app's native"
            )

            SettingsSwitchRow(
                setting = PrivateSettingsStore.hideBetaVersionWarning,
                title = "Hide beta version warning",
                description = "Hides the beta version warning in top of the adv settings screen"
            )

            SettingsSwitchRow(
                setting = PrivateSettingsStore.showSetDefaultLauncherBanner,
                title = "Show set default launcher banner",
                description = "If disabled, it won't appear if Dragon isn't the default launcher"
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.showFps,
                title = "Show FPS",
                description = "Display a FPS graph on top of everything"
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.showKillLauncherActionInActionPicker,
                title = "Show the kill launcher action in action selector",
                description = "If false, the kill launcher action is hidden"
            )

            SettingsSwitchRow(
                setting = UiSettingsStore.doNotRemindMeAgainPinLockWarning,
                title = "Do not remind me again Pin Lock",
                description = "Whether to show the pin code warning when setting a pin"
            )
        }

        DragonSettingsGroup(R.string.debug_infos) {
            SettingsSwitchRow(
                setting = DebugSettingsStore.debugInfos,
                title = stringResource(R.string.show_debug_infos),
                description = stringResource(R.string.show_debug_infos_desc)
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.settingsDebugInfo,
                title = stringResource(R.string.show_debug_infos_settings),
                description = stringResource(R.string.show_debug_infos_settings_desc)
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.widgetsDebugInfo,
                title = stringResource(R.string.show_debug_infos_widgets),
                description = stringResource(R.string.show_debug_infos_widgets_desc)
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.workspacesDebugInfo,
                title = stringResource(R.string.show_debug_infos_workspace),
                description = stringResource(R.string.show_debug_infos_workspace_desc)
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.privateSpaceDebugInfo,
                title = stringResource(R.string.private_space_debug_info),
                description = stringResource(R.string.private_space_debug_info_desc)
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.showDebugViewModel,
                title = "Show viewModels debug infos",
                description = "Displays a card that shows the hashCodes of all the view models in colors, if they change color, it is really bad, please report it to me if this is the case"
            )
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
            SettingsSwitchRow(
                setting = DebugSettingsStore.useAccessibilityInsteadOfContextToExpandActionPanel,
                title = stringResource(R.string.use_accessibility_instead_of_context),
                description = stringResource(R.string.use_accessibility_instead_of_context_desc)
            )

            SettingsSwitchRow(
                setting = DebugSettingsStore.autoRaiseDragonOnSystemLauncher,
                title = stringResource(R.string.auto_raise_dragon_on_system_launcher),
                description = stringResource(R.string.auto_raise_dragon_on_system_launcher_desc)
            )

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
                            pendingSystemLauncher = detectSystemLauncher(ctx)
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
                        "TikTok",
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
                            text = "Reset ${settingsStore.name}",
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
                    onClick = { scope.launch { initializationViewModel.initialize() } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = "Re-initialize points") }

                SettingsSwitchRow(
                    setting = DebugSettingsStore.disableExtensionSignatureCheck,
                    title = "Disable extension signature check",
                    description = "Allow extensions not signed with the official key"
                )
            }
        }
    }

    if (showEditAppOverrides) {
        PointIconEditor(
            point = dummySwipePoint(),
            onDismiss = { showEditAppOverrides = false }
        ) { newIcon ->
            appsViewModel.applyIconToApps(
                icon = newIcon
            )
            showEditAppOverrides = false
        }
    }
}