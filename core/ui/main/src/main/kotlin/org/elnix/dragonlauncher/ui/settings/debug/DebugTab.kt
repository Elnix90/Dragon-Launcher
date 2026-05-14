@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.debug

import android.content.Intent
import android.provider.Settings
import android.system.Os.kill
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.settings.allStores
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
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
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val appsViewModel = LocalAppsViewModel.current

    val scope = rememberCoroutineScope()

    val systemLauncherPackageName by DebugSettingsStore.systemLauncherPackageName.asState()

    var pendingSystemLauncher by remember { mutableStateOf<String?>(null) }
    var showEditAppOverrides by remember { mutableStateOf(false) }

    val debugInfosSectionState = rememberExpandableSection(stringResource(R.string.debug_infos))
    val packageSearchSectionState = rememberExpandableSection("Package Search")
    val storeResetSectionState = rememberExpandableSection(stringResource(R.string.store_reset))
    val dangerousActionsSectionState = rememberExpandableSection("Dangerous Actions")
    val testOverlaysSectionState = rememberExpandableSection("Test Overlays")
    val accessibilitySectionState = rememberExpandableSection("Accessibility & System")
    val uiDebugSectionState = rememberExpandableSection("UI & Flow Debug")

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

        ExpandableSection(uiDebugSectionState) {

            SettingsSwitchRow(
                setting = PrivateSettingsStore.hasSeenWelcome,
                title = "Has seen welcome",
                description = "Disabling that shows the welcome screen"
            )


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

            DragonButton(
                onClick = {
                    scope.launch {
                        PrivateSettingsStore.lastSeenVersionCodeWhatsNew.set(
                            ctx,
                            0
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Reset What's New sheet")
            }

            DragonButton(
                onClick = {
                    scope.launch {
                        PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.set(
                            ctx,
                            0
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Reset Google lockdown warning")
            }

            DragonButton(
                onClick = {
                    showEditAppOverrides = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Edit ALL app overrides \uD83D\uDE08")
            }

            DragonButton(
                onClick = {
                    @Suppress("DIVISION_BY_ZERO")
                    5 / 0
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "What is 5 / 0? \uD83E\uDD2F")
            }
        }

        ExpandableSection(debugInfosSectionState) {
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
        }

        ExpandableSection(packageSearchSectionState) {
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

        ExpandableSection(accessibilitySectionState) {
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
        ExpandableSection(testOverlaysSectionState) {
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

        ExpandableSection(storeResetSectionState) {
            allStores.entries.forEach { entry ->
                val settingsStore = entry.value
                OutlinedButton(
                    onClick = { scope.launch { settingsStore.resetAll(ctx) } },
                    colors = AppObjectsColors.cancelButtonColors(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
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
                colors = AppObjectsColors.cancelButtonColors(),
                onClick = { LifecycleUtils.closeApp(ctx as ComponentActivity) }
            ) {
                Text("Close app (gently)")
            }

            DragonButton(
                modifier = Modifier.fillMaxWidth(),
                colors = AppObjectsColors.cancelButtonColors(),
                onClick = { kill(9, 9) }
            ) {
                Text("☠\uFE0F Kill Process")
            }

            DragonButton(
                modifier = Modifier.fillMaxWidth(),
                colors = AppObjectsColors.cancelButtonColors(),
                onClick = {
                    ctx.startActivity(
                        Intent(Intent.ACTION_DELETE).apply {
                            data = "package:${ctx.packageName}".toUri()
                        }
                    )
                }
            ) {
                Text("☠\uFE0F Uninstall Launcher")
            }

            SettingsSwitchRow(
                setting = DebugSettingsStore.disableExtensionSignatureCheck,
                title = "Disable extension signature check",
                description = "Allow extensions not signed with the official key"
            )

            SettingsSwitchRow(
                setting = PrivateSettingsStore.hasInitialized,
                title = stringResource(R.string.has_initialized),
                description = "De-initializing will re-run the welcome flow. Will reset points",
                needValidationToDisable = true
            )
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