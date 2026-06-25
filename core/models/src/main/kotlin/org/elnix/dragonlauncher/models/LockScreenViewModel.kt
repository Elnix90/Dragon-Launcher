package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.SECURITY_SERVICE
import io.github.elnix90.logging.logD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.security.SecurityService
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import javax.inject.Inject

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    private val application: Application,
    private val securityService: SecurityService,
) : AndroidViewModel(application) {

    val isLocked = SettingFlow(false)
    val screenToUnlock = SettingFlow<NavigationRoute?>(null)

    private val lockMethod: Flow<LockMethod> = PrivateSettingsStore.lockMethod.flow(application)

    init {
        viewModelInitialized()
    }


    fun removeLock() {
        viewModelScope.launch {
            PrivateSettingsStore.lockPinHash.reset(application)
            PrivateSettingsStore.lockMethod.reset(application)
            unlock()
        }
    }

    fun setPinLockMethod(pin: String) {
        viewModelScope.launch {
            val hash = securityService.hashPin(pin)
            PrivateSettingsStore.lockPinHash.set(application, hash)
            PrivateSettingsStore.lockMethod.set(application, LockMethod.Pin)
            application.showToast(application.getString(R.string.pin_set_success))
            unlock()
        }
    }

    fun setLockScreenMethod() {
        viewModelScope.launch {
            PrivateSettingsStore.lockPinHash.reset(application)
            PrivateSettingsStore.lockMethod.set(application, LockMethod.Device)
            unlock()
        }
    }


    fun lock() {
        logD(SECURITY_SERVICE) { "User asked to lock!" }
        isLocked.value = true
    }

    fun unlock() {
        logD(SECURITY_SERVICE) { "User asked to unlock!" }
        isLocked.value = false
        screenToUnlock.value = null
    }

    fun cancelUnlock() {
        screenToUnlock.value = null
    }

    fun onEnterNewRoute(route: NavKey) {
        if (route !in NavigationRoute.settingsRoutes) {
            lock()
        }
    }


    fun requestUnlock(targetScreen: NavigationRoute) {
        viewModelScope.launch {
            when (lockMethod.first()) {
                LockMethod.None -> unlock()

                LockMethod.Pin -> {
                    screenToUnlock.value = targetScreen
                }

                LockMethod.Device -> {
                    screenToUnlock.value = targetScreen
                }
            }
        }
    }

    fun isDeviceUnlockAvailable(): Boolean = securityService.isDeviceUnlockAvailable(application)

    fun showDeviceUnlockPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) = securityService.showDeviceUnlockPrompt(
        activity = activity,
        onSuccess = onSuccess,
        onError = onError,
        onFailed = onFailed
    )

    fun verifyPin(pin: String, storedHash: String): Boolean = securityService.verifyPin(pin, storedHash)
}
