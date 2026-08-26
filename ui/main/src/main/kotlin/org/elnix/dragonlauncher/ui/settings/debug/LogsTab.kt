package org.elnix.dragonlauncher.ui.settings.debug

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logLevelName
import io.github.elnix90.runtime.asState
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.elnix.dragonlauncher.LOGS_TAG
import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.createShareableFile
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.shareContent
import org.elnix.dragonlauncher.base.utils.DateUtils.formatDateTime
import org.elnix.dragonlauncher.base.utils.detectSystemLauncher
import org.elnix.dragonlauncher.base.utils.rememberIsDefaultLauncher
import org.elnix.dragonlauncher.base.utils.rememberVersionCode
import org.elnix.dragonlauncher.base.utils.rememberVersionName
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.DragonLogViewModel
import org.elnix.dragonlauncher.services.ExtensionManager
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.CopyIcon
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import java.io.File

@Composable
fun LogsTab(dragonLogViewModel: DragonLogViewModel = activityViewModel()) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
//    val scope = rememberCoroutineScope()


    val enableLogging by DebugSettingsStore.enableLogging.asState()
//    val filterTag by DebugSettingsStore.filterTag.asState()

//    var tempFilterTag by remember(filterTag) { mutableStateOf(filterTag) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val logFiles by produceState(initialValue = emptyList(), ctx, refreshTrigger) {
        value = dragonLogViewModel.getAllLogFiles()
    }

    var showDeleteDialog by remember { mutableStateOf<File?>(null) }

    val windowInfo = LocalWindowInfo.current
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)
    val currentLauncher = ctx.detectSystemLauncher()
    val isDefault by rememberIsDefaultLauncher()
    val versionName by rememberVersionName()
    val versionCode by rememberVersionCode()

    // Build extension list by parsing the registry JSON directly (robust to field names)
    var finalExtensionText = "No extensions installed"
    try {
        val registryContent = ctx.assets.open("extensions-registry.json").bufferedReader().readText()
        val root = json.parseToJsonElement(registryContent)
        val lines = ArrayList<String>()

        if (root is JsonArray) {
            for (elem in root) {
                try {
                    val obj = elem.jsonObject
                    val pkgValue = obj["package"]?.jsonPrimitive?.contentOrNull
                    val nameValue = obj["name"]?.jsonPrimitive?.contentOrNull ?: "Unknown"

                    if (!pkgValue.isNullOrEmpty()) {
                        if (ExtensionManager.isExtensionInstalled(ctx, pkgValue)) {
                            val pkgInfo = try {
                                ctx.packageManager.getPackageInfo(pkgValue, 0)
                            } catch (_: Exception) {
                                null
                            }

                            val versionStr = pkgInfo?.versionName ?: "unknown"
                            lines.add("$nameValue ($versionStr)")
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }

        if (lines.isNotEmpty()) finalExtensionText = lines.joinToString("\n")
    } catch (_: Exception) {
        // registry not available or parse failed -> leave default text
    }

    val deviceDetails = remember {
        buildString {
            appendLine(" DEVICE DETAILS ")
            appendLine("System: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})")
            appendLine("OS: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            if (Build.VERSION.SECURITY_PATCH.isNotEmpty()) {
                appendLine("Security Patch: ${Build.VERSION.SECURITY_PATCH}")
            }
            appendLine("Arch: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"}")
            appendLine("Display: ${windowInfo.containerSize.width}x${windowInfo.containerSize.height}px")
            appendLine(
                "RAM: %.1fGB used / %.1fGB total (%d%% available)".format(
                    (memInfo.totalMem - memInfo.availMem) / 1024.0 / 1024 / 1024,
                    memInfo.totalMem / 1024.0 / 1024 / 1024,
                    memInfo.availMem * 100 / memInfo.totalMem
                )
            )
            appendLine("Default Launcher: ${if (isDefault) "Yes" else "No ($currentLauncher)"}")
            appendLine("App version: $versionName ($versionCode)")

            appendLine("\n EXTENSIONS ")
            appendLine(finalExtensionText)

            appendLine("\n PERMISSIONS ")
            try {
                val info = ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_PERMISSIONS)
                info.requestedPermissions?.forEachIndexed { index, perm ->
                    val flags = info.requestedPermissionsFlags
                    val granted = (flags != null && (flags[index] and 0x00000002) != 0) ||
                            ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
                    appendLine("${perm.substringAfterLast(".")}: ${if (granted) "✅" else "❌"}")
                }
            } catch (e: Exception) {
                appendLine("Error reading permissions: $e")
            }
        }
    }

    SettingsScaffold(
        title = "Logs",
        helpText = "Logs, need more info?",
        onReset = null,
        resetText = null,
        specialSettingsTitleContent = {
            AnimatedFab(
                onClick = {
                    refreshTrigger++
                    ctx.showToast("Refreshing...")
                },
                icon = R.drawable.refresh
            )
        },
    ) {
        DragonSettingsGroup {
            ExpandableSection(
                rememberExpandableSection(
                    title = R.string.device_info,
                    description = R.string.device_info_desc,
                    icon = R.drawable.bug_report
                )
            ) {
                DragonSettingsGroup {
                    DialogTitle(
                        text = stringResource(R.string.device_info),
                        modifier = Modifier.dragonSettingGroup(),
                        trailingIcon = {
                            CopyIcon {
                                ctx.copyToClipboard(deviceDetails)
                                ctx.showToast("Device info copied")
                            }
                        })
                    SelectionContainer(
                        modifier = Modifier.dragonSettingGroup()
                    ) {
                        Text(
                            text = deviceDetails,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Setting(DebugSettingsStore.enableLogging)
        }

        AnimatedVisibility(enableLogging) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                DragonSettingsGroup(R.string.log_level) {
                    Setting(
                        setting = DebugSettingsStore.snackBarLogLevel,
                        customDesc = { it.logLevelName }
                    )

                    Setting(
                        setting = DebugSettingsStore.filesLogLevel,
                        customDesc = { it.logLevelName }
                    )

                    Setting(DebugSettingsStore.filterTag, singleChar = false)
                    this.DragonButton(
                        onClick = {
                            dragonLogViewModel.clearLogs()
                            refreshTrigger++
                        },
                        needConfirm = true,
                        confirmText = "Are you sure you want to delete all logs files?"
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.delete_forever),
                            contentDescription = "Delete"
                        )
                        Spacer(8.dp)
                        Text("Clear All Logs")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                ) {
                    items(logFiles) { file ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigator.navigate(NavigationRoute.LogsViewer(file.name))
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column(modifier = Modifier.weight(1f)) {
                                    TextWithDescription(
                                        text = file.name,
                                        description = "${(file.length() / 1024).toInt()}KB • ${
                                            file.lastModified().formatDateTime()
                                        }"
                                    )
                                }

                                Row(
                                    modifier = Modifier.padding(5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    DragonIconButton(
                                        icon = R.drawable.delete_forever,
                                        contentDescription = R.string.delete
                                    ) { showDeleteDialog = file }

                                    DragonIconButton(
                                        onClick = {
                                            ctx.copyToClipboard(dragonLogViewModel.readLogFile(file))
                                        },
                                        icon = R.drawable.copy,
                                        contentDescription = R.string.copy
                                    )

                                    DragonIconButton(
                                        icon = R.drawable.share,
                                        contentDescription = R.string.export,
                                    ) { exportLogFile(dragonLogViewModel, ctx, file) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog != null) {
        val fileToDelete = showDeleteDialog!!

        UserValidation(
            title = "Delete file ${fileToDelete.name}",
            message = "THis can't be undone",
            onDismiss = { showDeleteDialog = null }
        ) {
            dragonLogViewModel.deleteLogFile(fileToDelete)
            refreshTrigger++
            showDeleteDialog = null
        }
    }
}

private fun exportLogFile(
    dragonLogViewModel: DragonLogViewModel,
    ctx: Context,
    file: File
) {
    try {
        val (shareFile, uri) = ctx.createShareableFile(file) ?: return

        ctx.shareContent(
            uri = uri,
            text = "Dragon Launcher logs",
            subject = "Dragon Logs - ${shareFile.name}",
            chooserTitle = "Share ${shareFile.name}"
        )

        logD(LOGS_TAG) { "Share opened: ${shareFile.name}" }

    } catch (e: SecurityException) {
        logE(LOGS_TAG, e) { "FileProvider not configured, falling back to text share" }

        // Fallback to text sharing
        val content = dragonLogViewModel.readLogFile(file)

        ctx.shareContent(
            text = content,
            subject = "Dragon Logs - ${file.name}",
            chooserTitle = "Share logs (text)"
        )
    } catch (e: Exception) {
        logE(LOGS_TAG, e) { "Failed to share log file" }
    }
}
