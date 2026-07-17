@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.debug

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.system.Os.kill
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.utils.LifecycleUtils
import org.elnix.dragonlauncher.common.utils.detectSystemLauncher
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.InitializationViewModel
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.settings.AllStores
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.timer.OverlayReminderService
import org.elnix.dragonlauncher.ui.base.UiConstants.dragonSettingGroupPaddingValues
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.dialogs.AppUsagePermissionDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
public fun DebugTab(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit,
    initializationViewModel: InitializationViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val systemLauncherPackageName by DebugSettingsStore.systemLauncherPackageName.asState()

    var pendingSystemLauncher by remember { mutableStateOf<String?>(null) }
//    var showEditAppOverrides by remember { mutableStateOf(false) }

    val storeResetSectionState = rememberExpandableSection(stringResource(R.string.store_reset))

    var packageQuery by remember { mutableStateOf("") }
    var packageResult by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }


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
        Setting(DebugSettingsStore.debugEnabled)

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
            contentPadding = dragonSettingGroupPaddingValues
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

            Setting(DebugSettingsStore.forceAppLanguageSelector)
            Setting(PrivateSettingsStore.hideBetaVersionWarning)
            Setting(PrivateSettingsStore.showSetDefaultLauncherBanner)
            Setting(DebugSettingsStore.showFps)
            Setting(DebugSettingsStore.showKillLauncherActionInActionPicker)
            Setting(UiSettingsStore.doNotRemindMeAgainPinLockWarning)
        }

        DragonSettingsGroup(R.string.debug_infos) {
            Setting(DebugSettingsStore.mainScreenDebugInfos)
            Setting(DebugSettingsStore.nestDebugInfo)
            Setting(DebugSettingsStore.nestDebugOverlay)
            Setting(DebugSettingsStore.cachesDebugOverlay)
            Setting(DebugSettingsStore.settingsDebugInfo)
            Setting(DebugSettingsStore.widgetsDebugInfo)
            Setting(DebugSettingsStore.workspacesDebugInfo)
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

                                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    info.longVersionCode
                                } else {
                                    @Suppress("DEPRECATION")
                                    info.versionCode
                                }
                                appendLine("Version: ${info.versionName} (${versionCode})")

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
            contentPadding = dragonSettingGroupPaddingValues
        ) {
            Setting(DebugSettingsStore.useAccessibilityInsteadOfContextToExpandActionPanel)
            Setting(DebugSettingsStore.autoRaiseDragonOnSystemLauncher)

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
            contentPadding = dragonSettingGroupPaddingValues
        ) {

            DragonButton(
                onClick = {
                    if (!Settings.canDrawOverlays(ctx)) {
                        showPermissionDialog = true
                        ctx.showToast("Overlay permission not granted")
                        return@DragonButton
                    }
                    OverlayReminderService.show(
                        ctx = ctx,
                        appName = "(Fuck) TikTok",
                        sessionTime = "15 min",
                        todayTime = "42 min",
                        remainingTime = "10 min",
                        hasLimit = true,
                        mode = "reminder"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Test: Reminder overlay")
            }

            DragonButton(
                onClick = {
                    if (!Settings.canDrawOverlays(ctx)) {
                        showPermissionDialog = true
                        ctx.showToast("Overlay permission not granted")
                        return@DragonButton
                    }
                    OverlayReminderService.show(
                        ctx = ctx,
                        appName = "(Fuck) TikTok",
                        sessionTime = "25 min",
                        todayTime = "58 min",
                        remainingTime = "5 min",
                        hasLimit = true,
                        mode = "time_warning"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Test: Limit overlay")
            }
        }

        DragonSettingsGroup(
            title = R.string.risky,
            contentPadding = dragonSettingGroupPaddingValues
        ) {

            DragonButton(
                onClick = {
                    @Suppress("DIVISION_BY_ZERO")
                    5 / 0
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(text = "What is 5 / 0? \uD83E\uDD2F") }

            DragonButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { LifecycleUtils.closeApp(ctx as ComponentActivity) }
            ) { Text("Close app (gently)") }

            DragonButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { kill(9, 9) }
            ) { Text("☠\uFE0F Kill Process") }

        }

        DragonSettingsGroup(
            title = R.string.dangerous_actions,
            contentPadding = dragonSettingGroupPaddingValues
        ) {
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

//            DragonButton(
//                onClick = {
//                    showEditAppOverrides = true
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) { Text(text = "Edit ALL app overrides \uD83D\uDE08") }

            DragonButton(
                onClick = {
                    PointStableCache.clear()
                    NestIntersectionShapesPathCache.clear()
                    initializationViewModel.initialize()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(text = "Re-initialize points") }

            Setting(DebugSettingsStore.disableExtensionSignatureCheck)

            ExpandableSection(storeResetSectionState) {
                AllStores.forEach { store ->
                    DragonButton(
                        onClick = { scope.launch { store.resetAll(ctx) } },
                        colors = AppObjectsColors.cancelButtonColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Reset ${store.name}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showPermissionDialog) {
        AppUsagePermissionDialog { showPermissionDialog = false }
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