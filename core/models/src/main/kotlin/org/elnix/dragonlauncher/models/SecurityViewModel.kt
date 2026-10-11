package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.Signatures.DRAGON_LAUNCHER_SIGNATURE_HASH
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Device
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Pattern
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Pin
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.checkSignature
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.security.SecurityService
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import javax.inject.Inject

@Stable
@HiltViewModel
class SecurityViewModel
	@Inject
	constructor(
		application: Application,
		private val securityService: SecurityService
	) : AndroidViewModel(application) {
		val isLocked: SettingFlow<Boolean> = SettingFlow(false)

		val signatureMatched: SettingFlow<Boolean> = SettingFlow(true)
		val useAnyways: SettingFlow<Boolean> = SettingFlow(false)

		init {
			signatureMatched.value = application.checkSignature(DRAGON_LAUNCHER_SIGNATURE_HASH)
			viewModelInitialized()
		}

		fun removeLock() {
			viewModelScope.launch {
				PrivateSettingsStore.settingsHash.reset(application)
				PrivateSettingsStore.lockMethod.reset(application)
				unlock()
			}
		}

		fun setPinLockMethod(pin: String) {
			viewModelScope.launch {
				val hash = securityService.hash(pin)
				PrivateSettingsStore.settingsHash.set(application, hash)
				PrivateSettingsStore.lockMethod.set(application, Pin)
				application.showToast(application.getString(R.string.pin_set_success))
				unlock()
			}
		}

		fun setPatternLockMethod(pattern: String) {
			viewModelScope.launch {
				val hash = securityService.hash(pattern)
				PrivateSettingsStore.settingsHash.set(application, hash)
				PrivateSettingsStore.lockMethod.set(application, Pattern)
				application.showToast(application.getString(R.string.pattern_set_successfully))
				unlock()
			}
		}

		fun setDeviceLockScreenMethod() {
			viewModelScope.launch {
				PrivateSettingsStore.settingsHash.reset(application)
				PrivateSettingsStore.lockMethod.set(application, Device)
				unlock()
			}
		}

		fun lock() {
			isLocked.value = true
		}

		fun unlock() {
			isLocked.value = false
		}

		suspend fun verify(pin: String): Boolean = securityService.verify(pin)

		fun onEnterNewRoute(route: NavKey) {
			if (route !in NavigationRoute.settingsRoutes) {
				lock()
			}
		}

		fun isDeviceUnlockAvailable(): Boolean = securityService.isDeviceUnlockAvailable(application)

		fun showDeviceUnlockPrompt(
			activity: FragmentActivity,
			onSuccess: () -> Unit,
			onError: (String) -> Unit,
			onFailed: () -> Unit
		): Unit =
			securityService.showDeviceUnlockPrompt(
				activity = activity,
				onSuccess = onSuccess,
				onError = onError,
				onFailed = onFailed
			)
	}
