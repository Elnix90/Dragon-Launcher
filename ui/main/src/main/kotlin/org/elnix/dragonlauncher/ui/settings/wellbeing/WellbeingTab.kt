@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.wellbeing

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.PackageNameLists.knownSocialMediaApps
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.ReminderMode
import org.elnix.dragonlauncher.enumsui.toggle.WellbeingPausedAppActions
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.AppLaunchViewModel
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.dialogs.AppPickerDialog
import org.elnix.dragonlauncher.ui.dialogs.AppUsagePermissionDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelectorRow
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
public fun WellbeingTab(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    appLaunchViewModel: AppLaunchViewModel = activityViewModel(),
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val socialMediaPauseEnabled by WellbeingSettingsStore.socialMediaPauseEnabled.asState()
    val pausedApps by WellbeingSettingsStore.pausedApps.asState()
    val reminderEnabled by WellbeingSettingsStore.reminderEnabled.asState()
    val reminderMode by WellbeingSettingsStore.reminderMode.asState()


    var showAppPicker by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }


    val allApps by drawerViewModel.allApps.collectAsState()
    val hasUsageStatsPermission by appLaunchViewModel.hasUsageStatsPermission.collectAsState()

    LaunchedEffect(reminderEnabled, reminderMode) {
        if (reminderEnabled && reminderMode == ReminderMode.Overlay && !Settings.canDrawOverlays(ctx)) {
            WellbeingSettingsStore.reminderEnabled.set(ctx, false)
            showOverlayPermissionDialog = true
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.wellbeing),
        helpText = stringResource(R.string.wellbeing_help),
        resetTitle = stringResource(R.string.reset_default_settings),
        resetText = stringResource(R.string.reset_settings_in_this_tab),
        onReset = {
            scope.launch {
                WellbeingSettingsStore.resetAll(ctx)
            }
        }
    ) {

        DragonSettingsGroup(R.string.social_media_pause) {
            Setting(WellbeingSettingsStore.socialMediaPauseEnabled)

            AnimatedVisibility(visible = socialMediaPauseEnabled) {
                Column {
                    Setting(
                        WellbeingSettingsStore.guiltModeEnabled,
                        enabled = true,
                    ) { newValue ->
                        if (newValue && hasUsageStatsPermission) {
                            showPermissionDialog = true
                        } else {
                            scope.launch {
                                WellbeingSettingsStore.guiltModeEnabled.set(ctx, newValue)
                            }
                        }
                    }

                    Setting(WellbeingSettingsStore.pauseDurationSeconds)
                }
            }
        }



        DragonSettingsGroup(R.string.reminder_mode_title) {
            Setting(
                WellbeingSettingsStore.reminderEnabled,
                enabled = socialMediaPauseEnabled,
            ) { newValue ->
                if (newValue && reminderMode == ReminderMode.Overlay && !Settings.canDrawOverlays(ctx)) {
                    showOverlayPermissionDialog = true
                }
            }

            AnimatedVisibility(visible = socialMediaPauseEnabled && reminderEnabled) {
                Setting(WellbeingSettingsStore.reminderIntervalMinutes)
            }
        }

        AnimatedVisibility(reminderMode == ReminderMode.Overlay && socialMediaPauseEnabled && reminderEnabled) {

            DragonSettingsGroup(R.string.popup_display_title) {
                ActionSelectorRow(
                    options = ReminderMode.entries,
                    selected = reminderMode,
                    label = stringResource(R.string.mode),
                    resetEnabled = reminderMode != ReminderMode.Overlay,
                    onReset = {
                        scope.launch {
                            WellbeingSettingsStore.reminderMode.reset(ctx)
                        }
                    }
                ) {
                    scope.launch {
                        WellbeingSettingsStore.reminderMode.set(ctx, it)
                    }
                }

                Setting(WellbeingSettingsStore.popupShowSessionTime)
                Setting(WellbeingSettingsStore.popupShowTodayTime)
                Setting(WellbeingSettingsStore.popupShowRemainingTime)
            }
        }

        DragonSettingsGroup(R.string.other) {
            Setting(WellbeingSettingsStore.returnToLauncherEnabled, enabled = socialMediaPauseEnabled)
        }

        DragonSettingsGroup(R.string.paused_apps) {
            MultiSelectConnectedButtonRow(
                entries = WellbeingPausedAppActions.entries,
                enabled = {
                    when (it) {
                        WellbeingPausedAppActions.Add, WellbeingPausedAppActions.AddAll -> true
                        WellbeingPausedAppActions.ClearAll -> pausedApps.isNotEmpty()
                    }
                }
            ) { action ->
                when (action) {
                    WellbeingPausedAppActions.Add -> {
                        showAppPicker = true
                    }

                    WellbeingPausedAppActions.AddAll -> {
                        scope.launch {
                            val installedPackages = allApps.map { it.packageName }.toSet()
                            val socialApps = knownSocialMediaApps.filter {
                                it in installedPackages
                            }
                            WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + socialApps)
                        }
                    }

                    WellbeingPausedAppActions.ClearAll -> {
                        scope.launch {
                            WellbeingSettingsStore.pausedApps.reset(ctx)
                        }
                    }
                }
            }

            if (pausedApps.isNotEmpty()) {

                pausedApps.forEach { packageName ->
                    val app = allApps.find { it.packageName == packageName }

                    app?.let {
                        PausedAppItem(
                            app = app,
                            onRemove = {
                                scope.launch {
                                    WellbeingSettingsStore.pausedApps.set(ctx, pausedApps - packageName)
                                }
                            }
                        )
                    }
                }
            } else {
                DragonColumnGroup {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🐉",
                            fontSize = 28.sp
                        )
                        Text(
                            text = stringResource(R.string.no_paused_apps),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        }
    }


    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                scope.launch {
                    WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + app.packageName)
                }
                showAppPicker = false
            }
        )
    }

    if (showPermissionDialog) {
        AppUsagePermissionDialog { showPermissionDialog = false }
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = { Text(stringResource(R.string.overlay_permission_required)) },
            text = { Text(stringResource(R.string.overlay_permission_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverlayPermissionDialog = false
                        ctx.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${ctx.packageName}".toUri()
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    }
                ) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PausedAppItem(
    app: Application,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {

            AppIcon(app, 30.dp)

            TextWithDescription(
                text = app.label,
                description = app.packageName
            )
        }
    }

    DragonIconButton(
        onClick = onRemove,
        icon = R.drawable.close,
        contentDescription = stringResource(R.string.remove),
        colors = AppObjectsColors.cancelIconButtonColors()
    )
}
