package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.ThemeObject
import org.elnix.dragonlauncher.common.loader.loadThemes
import org.elnix.dragonlauncher.common.utils.DateUtils.today
import org.elnix.dragonlauncher.enumsui.select.ExportImportTheme
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.THEMES_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.models.BackupResult
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.settings.SettingsBackupManager
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.settings.themeDataStores
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.components.BetaVersionType
import org.elnix.dragonlauncher.ui.components.BetaVersionWarning
import org.elnix.dragonlauncher.ui.dialogs.ThemeJsonPopup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsExportLauncher
import org.elnix.dragonlauncher.ui.remembers.rememberSettingsImportLauncher
import org.json.JSONObject

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ThemesTab(
    onBack: () -> Unit,
    backupViewModel: BackupViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val userThemesStore by UiSettingsStore.userThemes.asState()
    val userThemes: SnapshotStateSet<String> = remember { mutableStateSetOf() }

    LaunchedEffect(userThemesStore) {
        userThemes.addAll(userThemesStore)
    }


    var themes by remember { mutableStateOf<List<ThemeObject>?>(null) }

    LaunchedEffect(Unit) {
        themes = loadThemes(ctx)
    }


    var showJson by remember { mutableStateOf<JSONObject?>(null) }

    val settingsImportLauncher = rememberSettingsImportLauncher(
        onJsonReady = { json ->
            scope.launch {
                try {
                    ColorSettingsStore.backupColors(ctx)
                    ColorModesSettingsStore.colorTestMode.set(ctx, true)

                    SettingsBackupManager.importTheme(ctx, json)
                    backupViewModel.result.set(
                        BackupResult(
                            export = false,
                            error = false,
                            title = ctx.getString(R.string.import_successful)
                        )
                    )
                } catch (e: Exception) {
                    logE(BACKUP_TAG, e) { "Import failed" }

                    ColorSettingsStore.restoreColors(ctx)
                    ColorModesSettingsStore.colorTestMode.reset(ctx)

                    backupViewModel.result.set(
                        BackupResult(
                            export = false,
                            error = true,
                            title = ctx.getString(R.string.import_failed),
                            message = e.message ?: ""
                        )
                    )
                }
            }
        }
    )

    val settingsExportLauncher = rememberSettingsExportLauncher(themeDataStores)



    SettingsScaffold(
        title = stringResource(R.string.theme_selector),
        onBack = onBack,
        helpText = stringResource(R.string.theme_selector_help),
        onReset = null
    ) {
        BetaVersionWarning(BetaVersionType.Feature)

        SingleSelectConnectedButtonRow(
            entries = ExportImportTheme.entries,
            checked = { true }
        ) {
            when (it) {
                ExportImportTheme.Export -> {
                    settingsExportLauncher.launch("dragon_launcher_theme-${today()}.json")
                }

                ExportImportTheme.Import -> {
                    settingsImportLauncher.launch(
                        arrayOf(
                            "application/json",
                            "text/plain",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                }
            }
        }

        if (themes == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.loading_themes))
                Spacer(20.dp)
                LoadingIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 1000.dp)
            ) {

                themes?.let {
                    items(it) { theme ->
                        ThemeCard(
                            theme = theme,
                            onLongClick = { showJson = theme.json },
                            onClick = {
                                scope.launch {
                                    ColorSettingsStore.backupColors(ctx)
                                    ColorModesSettingsStore.colorTestMode.set(ctx, true)
                                    SettingsBackupManager.importTheme(ctx, theme.json)
                                }
                            }
                        )
                    }
                }
            }
        }

        fun addCurrentTheme() {

            scope.launch {
                val json = SettingsBackupManager.createJsonToExport(ctx, themeDataStores)

                userThemes.add(json.toString())
                UiSettingsStore.userThemes.set(ctx, userThemes)
            }
        }

        HorizontalDivider()

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 1000.dp)
        ) {

            item {
                DragonRow(
                    onClick = ::addCurrentTheme,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DragonIconButton(
                        onClick = ::addCurrentTheme,
                        icon = R.drawable.add,
                        contentDescription = stringResource(R.string.add_current_theme)
                    )
                    Text(stringResource(R.string.add_current_theme))
                }
            }
            userThemes.forEachIndexed { index, string ->
                val json = try {
                    JSONObject(string)
                } catch (e: Exception) {
                    logE(THEMES_TAG, e) { "Error decoding user theme json" }
                    JSONObject()
                }

                item {
                    UserThemeCard(
                        name = stringResource(R.string.user_theme, index),
                        onRemove = {
                            userThemes.remove(string)
                            scope.launch {
                                UiSettingsStore.userThemes.set(ctx, userThemes)
                            }
                        },
                        onLongClick = { showJson = json },
                        onClick = {
                            scope.launch {
                                ColorSettingsStore.backupColors(ctx)
                                ColorModesSettingsStore.colorTestMode.set(ctx, true)
                                SettingsBackupManager.importTheme(ctx, json)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showJson != null) {
        ThemeJsonPopup(showJson!!) { showJson = null }
    }
}

@Composable
private fun ThemeCard(
    theme: ThemeObject,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .shapedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Image(
            painter = if (theme.imageAssetPath != null) {
                rememberAssetPainter(theme.imageAssetPath!!)
            } else {
                painterResource(R.drawable.ic_app_default)
            },
            contentDescription = theme.name,
            modifier = Modifier
                .height(300.dp)
        )

        Spacer(5.dp)

        Text(
            text = theme.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun UserThemeCard(
    name: String,
    onRemove: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shapedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )

        DragonIconButton(
            onClick = onRemove,
            icon = R.drawable.remove_circle,
            contentDescription = stringResource(R.string.remove),
            colors = AppObjectsColors.cancelIconButtonColors()
        )
    }
}

@Composable
private fun rememberAssetPainter(assetPath: String): Painter {
    val ctx = LocalContext.current
    val bitmap = remember(assetPath) {
        ctx.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
    }
    return BitmapPainter(bitmap.asImageBitmap())
}
