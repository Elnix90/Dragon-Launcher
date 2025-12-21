@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.welcome

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.helpers.UserValidation
import org.elnix.dragonlauncher.ui.settings.backup.ImportSettingsDialog
import org.elnix.dragonlauncher.utils.SettingsBackupManager
import org.elnix.dragonlauncher.utils.models.BackupResult
import org.elnix.dragonlauncher.utils.models.BackupViewModel
import org.json.JSONObject

@Composable
fun WelcomeScreen(
    backupVm: BackupViewModel,
    onEnterSettings: () -> Unit,
    onEnterApp: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current


    var selectedStoresForImport by remember { mutableStateOf(listOf<DataStoreName>()) }
    var importJson by remember { mutableStateOf<JSONObject?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    val result by backupVm.result.collectAsState()

    // ------------------------------------------------------------
    // SETTINGS IMPORT LAUNCHER (File Picker)
    // ------------------------------------------------------------
    val settingsImportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            Log.d("BackupManager", "File picked: $uri")

            if (uri == null) {
                backupVm.setResult(
                    BackupResult(
                        export = false,
                        error = true,
                        message = ctx.getString(R.string.import_cancelled)
                    )
                )
                return@rememberLauncherForActivityResult
            }

            // Read JSON from selected file
            scope.launch {
                try {
                    val jsonString = withContext(Dispatchers.IO) {
                        ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }

                    if (jsonString.isNullOrBlank()) {
                        backupVm.setResult(
                            BackupResult(
                                export = false,
                                error = true,
                                message = "Invalid or empty backup file"
                            )
                        )
                        return@launch
                    }

                    importJson = JSONObject(jsonString)
                    showImportDialog = true

                } catch (e: Exception) {
                    backupVm.setResult(
                        BackupResult(
                            export = false,
                            error = true,
                            message = "Failed to read backup file: ${e.message}"
                        )
                    )
                }
            }
        }


    // Prevent the user to quit
    BackHandler { }

    fun setHasSeen() {
        scope.launch { PrivateSettingsStore.setHasSeenWelcome(ctx, true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets().asPaddingValues())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePageIntro { settingsImportLauncher.launch(arrayOf("application/json")) }
                    1 -> WelcomePagePrivacy()
                    2 -> WelcomePageTutorial()
                    3 -> WelcomePageTheme()
                    4 -> WelcomePageLauncher()
                    5 -> WelcomePageFinish(
                        onEnterSettings = {
                            setHasSeen()
                            onEnterSettings()
                        },
                        onEnterApp = {
                            setHasSeen()
                            onEnterApp()
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedPagerIndicator(
                currentPage = pagerState.currentPage,
                total = 6
            )
        }

        if (pagerState.currentPage < 5) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = {
                    val next = pagerState.currentPage + 1
                    if (next < 6) {
                        scope.launch { pagerState.animateScrollToPage(next) }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next"
                )
            }
        }
    }

    // Import Dialog (shows after file is picked)
    importJson?.let { json ->
        if (showImportDialog) {
            ImportSettingsDialog(
                backupJson = json,
                onDismiss = {
                    showImportDialog = false
                    importJson = null
                },
                onConfirm = { selectedStores ->
                    showImportDialog = false
                    selectedStoresForImport = selectedStores

                    scope.launch {
                        try {
                            SettingsBackupManager.importSettingsFromJson(ctx, json , selectedStoresForImport)
                            backupVm.setResult(BackupResult(export = false, error = false))
                            importJson = null
                        } catch (e: Exception) {
                            backupVm.setResult(
                                BackupResult(
                                    export = false,
                                    error = true,
                                    message = e.message ?: ""
                                )
                            )
                        }
                        setHasSeen()
                        onEnterApp()
                    }
                }
            )
        }
    }

    // ------------------------------------------------------------
    // RESULT DIALOG
    // ------------------------------------------------------------
    result?.let { res ->
        val isError = res.error
        val isExport = res.export
        val errorMessage = res.message

        UserValidation(
            title = when {
                isError && isExport -> stringResource(R.string.export_failed)
                isError && !isExport -> stringResource(R.string.import_failed)
                !isError && isExport -> stringResource(R.string.export_successful)
                else -> stringResource(R.string.import_successful)
            },
            message = when {
                isError -> errorMessage.ifBlank { stringResource(R.string.unknown_error) }
                isExport -> stringResource(R.string.export_successful)
                else -> stringResource(R.string.import_successful)
            },
            titleIcon = if (isError) Icons.Default.Warning else Icons.Default.Check,
            titleColor = if (isError) MaterialTheme.colorScheme.error else Color.Green,
            cancelText = null,
            copy = isError,
            onCancel = {},
            onAgree = { backupVm.setResult(null) }
        )
    }
}
