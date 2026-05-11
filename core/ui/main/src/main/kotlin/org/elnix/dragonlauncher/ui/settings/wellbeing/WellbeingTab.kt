@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.wellbeing

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.PackageNameLists.knownSocialMediaApps
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.utils.PermissionsUtils.hasUsageStatsPermission
import org.elnix.dragonlauncher.settings.stores.WellbeingSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.actions.appIcon
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.dialogs.AppPickerDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun WellbeingTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val appsViewModel = LocalAppsViewModel.current

    val scope = rememberCoroutineScope()

    val socialMediaPauseEnabled by WellbeingSettingsStore.socialMediaPauseEnabled.asState()
    val guiltModeEnabled by WellbeingSettingsStore.guiltModeEnabled.asState()
    val pauseDuration by WellbeingSettingsStore.pauseDurationSeconds.asState()
    val pausedApps by WellbeingSettingsStore.pausedApps.asState()

    val allApps by appsViewModel.allApps.collectAsState()

    var showAppPicker by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    val reminderEnabled by WellbeingSettingsStore.reminderEnabled.asState()
    val reminderInterval by WellbeingSettingsStore.reminderIntervalMinutes.asState()
    val reminderMode by WellbeingSettingsStore.reminderMode.asState()
    val returnToLauncherEnabled by WellbeingSettingsStore.returnToLauncherEnabled.asState()

    LaunchedEffect(reminderEnabled, reminderMode) {
        if (reminderEnabled && reminderMode == "overlay" && !Settings.canDrawOverlays(ctx)) {
            WellbeingSettingsStore.reminderEnabled.set(ctx, false)
            showOverlayPermissionDialog = true
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.wellbeing),
        onBack = onBack,
        helpText = stringResource(R.string.wellbeing_help),
        resetTitle = stringResource(R.string.reset_default_settings),
        resetText = stringResource(R.string.reset_settings_in_this_tab),
        onReset = {
            scope.launch {
                WellbeingSettingsStore.resetAll(ctx)
            }
        }
    ) {

        WellbeingHeroCard(
            pauseEnabled = socialMediaPauseEnabled,
            reminderEnabled = reminderEnabled,
            appCount = pausedApps.size
        )

        SectionHeader(
            icon = R.drawable.self_improvement,
            title = stringResource(R.string.social_media_pause)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .settingsGroup(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SettingsSwitchRow(
                setting = WellbeingSettingsStore.socialMediaPauseEnabled,
                title = stringResource(R.string.social_media_pause),
                description = stringResource(R.string.social_media_pause_description)
            )

            AnimatedVisibility(visible = socialMediaPauseEnabled) {
                Column {
                    SwitchRow(
                        state = guiltModeEnabled,
                        title = stringResource(R.string.guilt_mode),
                        description = stringResource(R.string.guilt_mode_description),
                        enabled = true,
                    ) { newValue ->
                        if (newValue && !hasUsageStatsPermission(ctx)) {
                            showPermissionDialog = true
                        } else {
                            scope.launch {
                                WellbeingSettingsStore.guiltModeEnabled.set(ctx, newValue)
                            }
                        }
                    }

                    SettingsSlider(
                        setting = WellbeingSettingsStore.pauseDurationSeconds,
                        title = stringResource(R.string.pause_duration),
                        description = stringResource(
                            R.string.pause_duration_description,
                            pauseDuration
                        ),
                        valueRange = 3..60,
                        allowTextEditValue = false,
                        showValue = false,
                        enabled = true
                    )
                }
            }
        }



        SectionHeader(
            icon = R.drawable.timer,
            title = stringResource(R.string.reminder_mode_title)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .settingsGroup(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SwitchRow(
                state = reminderEnabled,
                title = stringResource(R.string.reminder_mode_title),
                description = stringResource(R.string.reminder_mode_description),
                enabled = socialMediaPauseEnabled,
            ) { newValue ->
                if (newValue && reminderMode == "overlay" && !Settings.canDrawOverlays(ctx)) {
                    showOverlayPermissionDialog = true
                } else {
                    scope.launch {
                        WellbeingSettingsStore.reminderEnabled.set(ctx, newValue)
                    }
                }
            }

            AnimatedVisibility(visible = socialMediaPauseEnabled && reminderEnabled) {
                Column {
                    SettingsSlider(
                        setting = WellbeingSettingsStore.reminderIntervalMinutes,
                        title = stringResource(R.string.reminder_interval),
                        description = stringResource(
                            R.string.reminder_interval_description,
                            reminderInterval
                        ),
                        valueRange = 1..30,
                        allowTextEditValue = false,
                        showValue = false,
                        enabled = true
                    )

                    // Delivery mode picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModeChip(
                            label = stringResource(R.string.reminder_mode_notification),
                            icon = R.drawable.notification,
                            selected = reminderMode == "notification",
                            modifier = Modifier.weight(1f)
                        ) {
                            scope.launch {
                                WellbeingSettingsStore.reminderMode.set(ctx, "notification")
                            }
                        }

                        ModeChip(
                            label = stringResource(R.string.reminder_mode_overlay),
                            icon = R.drawable.layers,
                            selected = reminderMode == "overlay",
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!Settings.canDrawOverlays(ctx)) {
                                showOverlayPermissionDialog = true
                            } else {
                                scope.launch {
                                    WellbeingSettingsStore.reminderMode.set(ctx, "overlay")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        AnimatedVisibility(reminderMode == "overlay" && socialMediaPauseEnabled && reminderEnabled) {

            SectionHeader(
                icon = R.drawable.visibility,
                title = stringResource(R.string.popup_display_title)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .settingsGroup(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                SettingsSwitchRow(
                    setting = WellbeingSettingsStore.popupShowSessionTime,
                    title = stringResource(R.string.popup_show_session_time),
                    description = stringResource(R.string.popup_show_session_time_desc)
                )
                SettingsSwitchRow(
                    setting = WellbeingSettingsStore.popupShowTodayTime,
                    title = stringResource(R.string.popup_show_today_time),
                    description = stringResource(R.string.popup_show_today_time_desc)
                )
                SettingsSwitchRow(
                    setting = WellbeingSettingsStore.popupShowRemainingTime,
                    title = stringResource(R.string.popup_show_remaining_time),
                    description = stringResource(R.string.popup_show_remaining_time_desc)
                )
            }
        }


        SectionHeader(
            icon = R.drawable.exit_to_app,
            title = stringResource(R.string.return_to_launcher_title)
        )

        SwitchRow(
            state = returnToLauncherEnabled,
            title = stringResource(R.string.return_to_launcher_title),
            description = stringResource(R.string.return_to_launcher_description),
            enabled = socialMediaPauseEnabled,
        ) { newValue ->
            scope.launch {
                WellbeingSettingsStore.returnToLauncherEnabled.set(ctx, newValue)
            }
        }

        SectionHeader(
            icon = R.drawable.schedule,
            title = if (pausedApps.isNotEmpty())
                "${stringResource(R.string.paused_apps)} (${pausedApps.size})"
            else
                stringResource(R.string.paused_apps)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsItem(
                title = stringResource(R.string.add_app),
                icon = R.drawable.add,
                modifier = Modifier.weight(1f),
                enabled = socialMediaPauseEnabled
            ) { showAppPicker = true }

            SettingsItem(
                title = stringResource(R.string.add_social_media),
                icon = R.drawable.ic_app_grid,
                modifier = Modifier.weight(1f),
                enabled = socialMediaPauseEnabled
            ) {
                scope.launch {
                    val installedPackages = allApps.map { it.packageName }.toSet()
                    val socialApps = knownSocialMediaApps.filter {
                        it in installedPackages
                    }
                    WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + socialApps)
                }
            }
        }

        AnimatedVisibility(visible = pausedApps.isNotEmpty()) {
            SettingsItem(
                title = stringResource(R.string.clear_all),
                icon = R.drawable.backspace,
                enabled = socialMediaPauseEnabled
            ) {
                scope.launch {
                    WellbeingSettingsStore.pausedApps.reset(ctx)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DragonShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, DragonShape)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
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
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.usage_permission_required)) },
            text = { Text(stringResource(R.string.usage_permission_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                ) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Components
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
private fun WellbeingHeroCard(
    pauseEnabled: Boolean,
    reminderEnabled: Boolean,
    appCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DragonShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, DragonShape)
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "🐉", fontSize = 28.sp)
                Column {
                    Text(
                        text = stringResource(R.string.wellbeing),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.wellbeing_help),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }

            // Status pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPill(
                    label = stringResource(R.string.social_media_pause),
                    active = pauseEnabled,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = stringResource(R.string.reminder_mode_title),
                    active = reminderEnabled,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = if (appCount > 0) "$appCount apps" else "0",
                    active = appCount > 0,
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pillBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pillText"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 1,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SectionHeader(
    icon: Int,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ModeChip(
    label: String,
    icon: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "chipBorder"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun PausedAppItem(
    app: AppModel,
    onRemove: () -> Unit
) {
    val iconShape = LocalIconShape.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DragonShape)
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

            Image(
                painter = appIcon(app),
                contentDescription = app.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(iconShape.resolveShape()),
                contentScale = ContentScale.Fit
            )

            Column {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    maxLines = 1
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
}
