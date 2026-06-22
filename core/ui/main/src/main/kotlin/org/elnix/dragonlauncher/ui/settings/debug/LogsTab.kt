@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.debug

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import androidx.compose.material3.TextField
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.createShareableFile
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.shareContent
import org.elnix.dragonlauncher.common.utils.DateUtils.formatDateTime
import org.elnix.dragonlauncher.common.utils.detectSystemLauncher
import org.elnix.dragonlauncher.common.utils.rememberIsDefaultLauncher
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.common.utils.rememberVersionName
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import io.github.elnix90.logging.LOGS_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logLevelName
import org.elnix.dragonlauncher.models.DragonLogViewModel
import org.elnix.dragonlauncher.services.ExtensionManager
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.activityViewModel
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import java.io.File

@Composable
fun LogsTab(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit,
    dragonLogViewModel: DragonLogViewModel = activityViewModel()
) {
    val ctx = LocalContext.current

    val enableLogging by DebugSettingsStore.enableLogging.asState()
    val snackBarLogLevel by DebugSettingsStore.snackBarLogLevel.asState()
    val filesLogLevel by DebugSettingsStore.snackBarLogLevel.asState()
    val filterTag by DebugSettingsStore.filterTag.asState()

    var tempFilterTag by remember(filterTag) { mutableStateOf(filterTag) }

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
        val root = Json.parseToJsonElement(registryContent)
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
            appendLine("─── DEVICE DETAILS ───")
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

            appendLine("\n─── EXTENSIONS ───")
            appendLine(finalExtensionText)

            appendLine("\n─── PERMISSIONS ───")
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
        onBack = onBack,
        helpText = "Logs, need more info?",
        onReset = null,
        otherIcons = arrayOf(
            Triple(
                { refreshTrigger++; ctx.showToast("Refreshing...") },
                R.drawable.refresh,
                stringResource(R.string.refresh)
            )
        )
    ) {
        ExpandableSection(rememberExpandableSection("Device info")) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = AppObjectsColors.cardColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Device Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DragonIconButton(
                            onClick = {
                                ctx.copyToClipboard(deviceDetails)
                                ctx.showToast("Device info copied")
                            },
                            icon = R.drawable.copy,
                            contentDescription = "Copy Info"
                        )
                    }
                    Spacer(8.dp)
                    SelectionContainer {
                        Text(
                            text = deviceDetails,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        SettingsSwitchRow(DebugSettingsStore.enableLogging)

        AnimatedVisibility(enableLogging) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SliderWithLabel(
                    label = stringResource(DebugSettingsStore.snackBarLogLevel.title!!),
                    description = snackBarLogLevel.logLevelName,
                    value = snackBarLogLevel,
                    showValue = false,
                    allowTextEditValue = false,
                    valueRange = 2..7,
                    onReset = {
                        dragonLogViewModel.updateSnackBarLogLevel(Log.ERROR)
                    }
                ) {
                    dragonLogViewModel.updateSnackBarLogLevel(it)
                }

                SliderWithLabel(
                    label = stringResource(DebugSettingsStore.filesLogLevel.title!!),
                    description = filesLogLevel.logLevelName,
                    value = filesLogLevel,
                    showValue = false,
                    allowTextEditValue = false,
                    valueRange = 2..7,
                    onReset = {
                        dragonLogViewModel.updateFilesLogLevel(Log.DEBUG)
                    }
                ) {
                    dragonLogViewModel.updateFilesLogLevel(it)
                }

                TextField(
                    value = tempFilterTag,
                    onValueChange = {
                        tempFilterTag = it
                        dragonLogViewModel.updateFilterTag(it)
                    },
                    label = { Text(stringResource(R.string.filter_tag)) },
                    colors = AppObjectsColors.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(1f)
                )

                DragonButton(
                    onClick = {
                        dragonLogViewModel.clearLogs()
                        refreshTrigger++
                    },
                    modifier = Modifier.padding(16.dp),
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
                                    onNavigate(NavigationRoute.LogsViewer(file.name))
                                },
                            colors = AppObjectsColors.cardColors()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp),
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
                                        contentDescription = "Delete"
                                    ) { showDeleteDialog = file }

                                    DragonIconButton(
                                        onClick = {
                                            ctx.copyToClipboard(dragonLogViewModel.readLogFile(file))
                                        },
                                        icon = R.drawable.copy,
                                        contentDescription = "Copy"
                                    )

                                    DragonIconButton(
                                        icon = R.drawable.share,
                                        contentDescription = "Export",
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
