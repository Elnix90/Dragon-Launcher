@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.welcome

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `in`.hridayan.shapeindicators.ShapeIndicatorDefaults
import `in`.hridayan.shapeindicators.ShapeIndicatorRow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BACKUP_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.WELCOME_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.settings.SettingsBackupManager
import org.elnix.dragonlauncher.settings.bases.DatastoreProvider
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.composition.LocalBackupViewModel
import org.elnix.dragonlauncher.ui.dialogs.ImportSettingsDialog
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsImportLauncher
import org.json.JSONObject


private const val pageNumber = 6

@SuppressLint("LocalContextGetResourceValueCall", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(
    onEnterSettings: () -> Unit,
    onEnterApp: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pageNumber })
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val backupViewModel = LocalBackupViewModel.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    val pagerPage = PrivateSettingsStore.welcomeScreenTempPage.getOrNull(ctx)

                    if (pagerPage != null) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerPage)
                        }
                    }
                }
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(pagerState.currentPage) {
        val pageId = pagerState.currentPage

        logD(WELCOME_TAG) { "Setting the pager to $pageId" }
        // Set the current page to remember it
        scope.launch {
            PrivateSettingsStore.welcomeScreenTempPage.set(ctx, pageId)
        }
    }

    var selectedStoresForImport by remember { mutableStateOf(setOf<DatastoreProvider>()) }
    var importJson by remember { mutableStateOf<JSONObject?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val settingsImportLauncher = rememberSettingsImportLauncher(
        onJsonReady = { json ->
            importJson = json
            showImportDialog = true
        }
    )

    // Prevent the user to quit
    BackHandler { }

    fun setHasSeen() {
        scope.launch {
            with(PrivateSettingsStore) {
                hasSeenWelcome.set(ctx, true)
                // Resets the pager, that is only used to scroll to the page the user left when it re-enters the welcome screen
                welcomeScreenTempPage.reset(ctx)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
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
                    0 -> WelcomePageIntro({ pagerState.currentPage < 2 }) {
                        settingsImportLauncher.launch(
                            arrayOf(
                                "application/json",
                                "text/plain",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    }

                    1 -> WelcomePagePrivacy()
                    2 -> WelcomePageTutorial()
                    3 -> WelcomePageLauncher()
                    4 -> WelcomePageBackup()
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

            Spacer(16.dp)

            ShapeIndicatorRow(
                pagerState = pagerState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                shuffleShapes = true,
                overflow = ShapeIndicatorDefaults.overflow(maxVisibleItems = pageNumber)
            )
        }

        val fabScale by animateFloatAsState(
            if (pagerState.currentPage < pageNumber - 1) 1f else -(pagerState.currentPageOffsetFraction * 2)
        )
        AnimatedFab(
            icon = R.drawable.arrow_forward,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.BottomEnd)
                .scale(fabScale),
            minSize = 80.dp
        ) {
            val next = pagerState.currentPage + 1
            if (next < pageNumber) {
                scope.launch { pagerState.animateScrollToPage(next) }
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
                    selectedStoresForImport = selectedStores.keys

                    scope.launch {
                        try {
                            SettingsBackupManager.importSettingsFromJson(ctx, json, selectedStoresForImport)
                            backupViewModel.setResult(
                                BackupResult(
                                    export = false,
                                    error = false,
                                    title = ctx.getString(R.string.import_successful)
                                )
                            )
                            importJson = null
                        } catch (e: Exception) {
                            logE(BACKUP_TAG, e) { "Import Failed" }
                            backupViewModel.setResult(
                                BackupResult(
                                    export = false,
                                    error = true,
                                    title = ctx.getString(R.string.import_failed),
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
}
