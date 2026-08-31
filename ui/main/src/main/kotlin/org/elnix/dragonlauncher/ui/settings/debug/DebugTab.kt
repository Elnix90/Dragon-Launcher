package org.elnix.dragonlauncher.ui.settings.debug

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.system.Os.kill
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.utils.LifecycleUtils
import org.elnix.dragonlauncher.base.utils.detectSystemLauncher
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.settings.AllStores
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.timer.OverlayReminderService
import org.elnix.dragonlauncher.ui.base.animation.Icon
import org.elnix.dragonlauncher.ui.base.animation.rememberAnimatedIcon
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dialogs.AppUsagePermissionDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.settings.RouteItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun DebugTab() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val storeResetSectionState =
        rememberExpandableSection(
            R.string.store_reset,
            description = R.string.store_reset,
            icon = R.drawable.delete_forever
        )

    var packageResult by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.debug),
        helpText = "Advanced developer tools and system overrides.",
        onReset = null,
        resetText = null
    ) {
        DragonSettingsGroup { Setting(DebugSettingsStore.debugEnabled) }

        DragonSettingsGroup(R.string.more) {
            RouteItem(NavigationRoute.Logs)
            RouteItem(NavigationRoute.SettingsJson)
        }

        DragonSettingsGroup(R.string.ui_flow_and_debug) {
            this.DragonButton(onClick = { scope.launch { PrivateSettingsStore.lastSeenVersionCodeWhatsNew.reset(ctx) } }) {
                Text(text = "Show What's New sheet")
            }

            this.DragonButton(onClick = { scope.launch { PrivateSettingsStore.lastSeenVersionCodeGoogleLockdownWarning.reset(ctx) } }) {
                Text(text = "Show Google lockdown warning")
            }

            this.DragonButton(onClick = { scope.launch { PrivateSettingsStore.hasSeenWelcome.reset(ctx) } }) {
                Text(text = "Show Welcome Screen")
            }

            Setting(DebugSettingsStore.forceAppLanguageSelector)
            Setting(PrivateSettingsStore.hideBetaVersionWarning)
            Setting(PrivateSettingsStore.showSetDefaultLauncherBanner)
            Setting(PrivateSettingsStore.showReselectBackupBanner)
            Setting(DebugSettingsStore.showFps)
            Setting(DebugSettingsStore.showKillLauncherActionInActionPicker)
            Setting(UiSettingsStore.doNotRemindMeAgainPinLockWarning)
        }

        DragonSettingsGroup(R.string.debug_infos) {
            Setting(DebugSettingsStore.mainScreenDebugInfos)
            Setting(DebugSettingsStore.nestDebugInfo)
            Setting(DebugSettingsStore.nestDebugOverlay)
//            Setting(DebugSettingsStore.cachesDebugOverlay)
            Setting(DebugSettingsStore.settingsDebugInfo)
            Setting(DebugSettingsStore.widgetsDebugInfo)
            Setting(DebugSettingsStore.workspacesDebugInfo)
        }

        DragonSettingsGroup(R.string.package_search) {
            val focusManager = LocalFocusManager.current
            val animatedIcon = rememberAnimatedIcon()
            var packageQuery by remember { mutableStateOf("") }

            fun searchPackage() {
                packageResult =
                    try {
                        val info = ctx.packageManager.getPackageInfo(packageQuery.trim(), 0)
                        animatedIcon.setSuccess()
                        buildString {
                            appendLine("Package: ${info.packageName}")

                            val versionCode =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    info.longVersionCode
                                } else {
                                    @Suppress("DEPRECATION")
                                    info.versionCode
                                }
                            appendLine("Version: ${info.versionName} ($versionCode)")

                            appendLine("Enabled: ${info.applicationInfo?.enabled ?: "unknown"}")
                            appendLine("Data Dir: ${info.applicationInfo?.dataDir ?: "unknown"}")
                        }
                    } catch (e: Exception) {
                        animatedIcon.setError()
                        "Not found or error: $e"
                    }
            }

            TextField(
                value = packageQuery,
                onValueChange = { packageQuery = it },
                placeholder = { Text("Search package") },
                colors =
                    AppObjectsColors.outlinedTextFieldColors(
                        removeBorder = true
                    ),
                shape = CircleShape,
                modifier = Modifier.dragonSettingGroup(),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            searchPackage()
                            focusManager.clearFocus()
                        }
                    ),
                trailingIcon = {
                    animatedIcon.Icon(
                        defaultIcon = R.drawable.search,
                        enabled = packageQuery.isNotEmpty()
                    ) {
                        focusManager.clearFocus()
                        searchPackage()
                    }
                }
            )

            packageResult?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.dragonSettingGroup()
                )
            }
        }

        DragonSettingsGroup(R.string.accessibility) {
            Setting(DebugSettingsStore.useAccessibilityInsteadOfContextToExpandActionPanel)
            Setting(DebugSettingsStore.autoRaiseDragonOnSystemLauncher)

            this.DragonButton(onClick = { SystemControl.openServiceSettings((ctx)) }) {
                Text("Open Accessibility Services")
            }
        }

        DragonSettingsGroup(R.string.system) {
            val focusManager = LocalFocusManager.current
            val animatedIcon = rememberAnimatedIcon()

            var customSystemPackage by remember { mutableStateOf("") }

            fun setSystemPackage() {
                scope.launch {
                    DebugSettingsStore.systemLauncherPackageName.set(ctx, customSystemPackage)
                }
                animatedIcon.setSuccess()
            }

            val systemLauncherPackageNameSetting by DebugSettingsStore.systemLauncherPackageName.asState()
            val systemLauncherPackageName = remember { ctx.detectSystemLauncher() }

            val setButtonEnabled = systemLauncherPackageName != systemLauncherPackageNameSetting
            DragonButton(
                onClick = {
                    scope.launch {
                        DebugSettingsStore.systemLauncherPackageName.set(ctx, systemLauncherPackageName)
                    }
                },
                enabled = setButtonEnabled
            ) {
                AnimatedContent(setButtonEnabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (it) {
                            Icon(
                                painter = painterResource(R.drawable.save),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(5.dp)
                            Text("Set Detected Launcher")
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(5.dp)
                            Text("Detected In use!")
                        }
                    }
                }
            }

            TextWithDescription(
                text = "Detected system launcher:",
                description = systemLauncherPackageName ?: "unknown",
                modifier = Modifier.dragonSettingGroup()
            )

            TextField(
                value = customSystemPackage,
                onValueChange = { customSystemPackage = it },
                placeholder = { Text("System launcher package") },
                colors =
                    AppObjectsColors.outlinedTextFieldColors(
                        removeBorder = true
                    ),
                shape = CircleShape,
                modifier = Modifier.dragonSettingGroup(),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            setSystemPackage()
                            focusManager.clearFocus()
                        }
                    ),
                trailingIcon = {
                    animatedIcon.Icon(
                        defaultIcon = R.drawable.search,
                        enabled = customSystemPackage.isNotEmpty()
                    ) {
                        setSystemPackage()
                        focusManager.clearFocus()
                    }
                }
            )
        }

        DragonSettingsGroup(R.string.test_overlays) {
            this.DragonButton(
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
                }
            ) {
                Text(text = "Test: Reminder overlay")
            }

            this.DragonButton(
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
                }
            ) {
                Text(text = "Test: Limit overlay")
            }
        }

        DragonSettingsGroup(R.string.risky) {
            this.DragonButton(
                onClick = {
                    @Suppress("DIVISION_BY_ZERO")
                    5 / 0
                }
            ) { Text(text = "What is 5 / 0? \uD83E\uDD2F") }

            this.DragonButton(onClick = { LifecycleUtils.closeApp(ctx as ComponentActivity) }) { Text("Close app (gently)") }
            this.DragonButton(onClick = { kill(9, 9) }) { Text("☠\uFE0F Kill Process") }
        }

        DragonSettingsGroup(R.string.dangerous_actions) {
            this.DragonButton(
                onClick = {
                    ctx.startActivity(
                        Intent(Intent.ACTION_DELETE).apply {
                            data = "package:${ctx.packageName}".toUri()
                        }
                    )
                }
            ) { Text("☠\uFE0F Uninstall Launcher") }

            Setting(DebugSettingsStore.disableExtensionSignatureCheck)

            ExpandableSection(storeResetSectionState) {
                AllStores.forEach { store ->
                    DragonButton(
                        onClick = { scope.launch { store.resetAll(ctx) } },
                        isCancel = true
                    ) {
                        Text("Reset ${store.name}")
                    }
                }
            }
        }
    }

    if (showPermissionDialog) {
        AppUsagePermissionDialog { showPermissionDialog = false }
    }
}
