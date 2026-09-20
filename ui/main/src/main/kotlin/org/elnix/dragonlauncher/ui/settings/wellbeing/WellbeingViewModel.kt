package org.elnix.dragonlauncher.ui.settings.wellbeing

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.PackageNameLists.knownSocialMediaApps
import org.elnix.dragonlauncher.base.model.models.ReminderMode
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore

/**
 * Screen-scoped state and logic for [WellbeingTab].
 * Dialog visibility, the pending reminder intent and every settings write
 * live here so the tab only declares UI.
 */
@HiltViewModel
@Stable
public class WellbeingViewModel
    @Inject
    constructor(
        application: Application
    ) : AndroidViewModel(application) {
        public val showAppPicker = mutableStateOf(false)
        public val showPermissionDialog = mutableStateOf(false)
        public val showOverlayPermissionDialog = mutableStateOf(false)

        /**
         * Remembers that the user asked for overlay reminders while the
         * overlay permission was missing, so the toggle can be restored
         * once the permission is granted.
         */
        public val pendingReminderEnable = mutableStateOf(false)

        /** Setting() persists the value itself; only guide to system settings when needed. */
        public fun onGuiltToggle(newValue: Boolean, hasUsageStatsPermission: Boolean) {
            if (newValue && !hasUsageStatsPermission) {
                showPermissionDialog.value = true
            }
        }

        public fun onReminderToggle(newValue: Boolean, reminderMode: ReminderMode) {
            val ctx = getApplication<Application>()
            if (newValue && reminderMode == ReminderMode.Overlay && !Settings.canDrawOverlays(ctx)) {
                pendingReminderEnable.value = true
                showOverlayPermissionDialog.value = true
            }
        }

        /**
         * Reconciles the reminder toggle with the overlay permission, called
         * whenever the toggle, the mode or the permission state changes.
         */
        public fun syncOverlayState(reminderEnabled: Boolean, reminderMode: ReminderMode, canShowOverlay: Boolean) {
            val ctx = getApplication<Application>()
            viewModelScope.launch {
                if (reminderMode == ReminderMode.Overlay && !canShowOverlay) {
                    if (reminderEnabled) {
                        WellbeingSettingsStore.reminderEnabled.set(ctx, false)
                        // The toggle itself already showed the dialog in this case.
                        if (!pendingReminderEnable.value) showOverlayPermissionDialog.value = true
                    }
                } else if (pendingReminderEnable.value && canShowOverlay) {
                    pendingReminderEnable.value = false
                    WellbeingSettingsStore.reminderEnabled.set(ctx, true)
                }
            }
        }

        public fun onOverlayDialogConfirm() {
            val ctx = getApplication<Application>()
            showOverlayPermissionDialog.value = false
            ctx.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${ctx.packageName}".toUri()
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }

        public fun onOverlayDialogDismiss() {
            pendingReminderEnable.value = false
            showOverlayPermissionDialog.value = false
        }

        public fun onAddSocialMedia(allPackages: Set<String>, pausedApps: Set<String>) {
            val ctx = getApplication<Application>()
            viewModelScope.launch {
                val socialApps = knownSocialMediaApps.filter { it in allPackages }
                WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + socialApps)
            }
        }

        public fun onRemovePausedApp(packageName: String, pausedApps: Set<String>) {
            val ctx = getApplication<Application>()
            viewModelScope.launch {
                WellbeingSettingsStore.pausedApps.set(ctx, pausedApps - packageName)
            }
        }

        public fun onAppPicked(packageName: String, pausedApps: Set<String>) {
            val ctx = getApplication<Application>()
            viewModelScope.launch {
                WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + packageName)
                showAppPicker.value = false
            }
        }

        public fun onMultipleAppsPicked(packageNames: Set<String>, pausedApps: Set<String>) {
            val ctx = getApplication<Application>()
            viewModelScope.launch {
                WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + packageNames)
                showAppPicker.value = false
            }
        }

        public fun onResetSettings() {
            val ctx = getApplication<Application>()
            viewModelScope.launch {
                WellbeingSettingsStore.resetAll(ctx)
            }
        }
    }
