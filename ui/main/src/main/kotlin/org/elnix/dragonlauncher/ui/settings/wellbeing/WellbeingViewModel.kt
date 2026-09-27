package org.elnix.dragonlauncher.ui.settings.wellbeing

import android.app.Application
import android.provider.Settings
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.PackageNameLists.knownSocialMediaApps
import org.elnix.dragonlauncher.base.model.models.ReminderMode
import org.elnix.dragonlauncher.ktx.openOverlaySettings
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore

/**
 * Screen-scoped state and logic for [WellbeingTab].
 * Dialog visibility, the pending reminder intent and every settings write
 * live here so the tab only declares UI.
 */
@HiltViewModel
@Stable
class WellbeingViewModel
	@Inject
	constructor(
		application: Application
	) : AndroidViewModel(application) {
		val showAppPicker = mutableStateOf(false)
		val showPermissionDialog = mutableStateOf(false)
		val showOverlayPermissionDialog = mutableStateOf(false)

		/**
		 * Remembers that the user asked for overlay reminders while the
		 * overlay permission was missing, so the toggle can be restored
		 * once the permission is granted.
		 */
		val pendingReminderEnable = mutableStateOf(false)

		/** Setting() persists the value itself; only guide to system settings when needed. */
		fun onGuiltToggle(newValue: Boolean, hasUsageStatsPermission: Boolean) {
			if (newValue && !hasUsageStatsPermission) {
				showPermissionDialog.value = true
			}
		}

		fun onReminderToggle(newValue: Boolean, reminderMode: ReminderMode) {
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
		fun syncOverlayState(reminderEnabled: Boolean, reminderMode: ReminderMode, canShowOverlay: Boolean) {
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

		fun onOverlayDialogConfirm() {
			showOverlayPermissionDialog.value = false
			application.openOverlaySettings()
		}

		fun onOverlayDialogDismiss() {
			pendingReminderEnable.value = false
			showOverlayPermissionDialog.value = false
		}

		fun onAddSocialMedia(allPackages: Set<String>, pausedApps: Set<String>) {
			val ctx = getApplication<Application>()
			viewModelScope.launch {
				val socialApps = knownSocialMediaApps.filter { it in allPackages }
				WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + socialApps)
			}
		}

		fun onRemovePausedApp(packageName: String, pausedApps: Set<String>) {
			val ctx = getApplication<Application>()
			viewModelScope.launch {
				WellbeingSettingsStore.pausedApps.set(ctx, pausedApps - packageName)
			}
		}

		fun onAppPicked(packageName: String, pausedApps: Set<String>) {
			val ctx = getApplication<Application>()
			viewModelScope.launch {
				WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + packageName)
				showAppPicker.value = false
			}
		}

		fun onMultipleAppsPicked(packageNames: Set<String>, pausedApps: Set<String>) {
			val ctx = getApplication<Application>()
			viewModelScope.launch {
				WellbeingSettingsStore.pausedApps.set(ctx, pausedApps + packageNames)
				showAppPicker.value = false
			}
		}

		fun onResetSettings() {
			val ctx = getApplication<Application>()
			viewModelScope.launch {
				WellbeingSettingsStore.resetAll(ctx)
			}
		}
	}
