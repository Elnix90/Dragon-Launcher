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
public class LockScreenViewModel @Inject constructor(
    private val application: Application,
    private val securityService: SecurityService,
) : AndroidViewModel(application) {

    public val isLocked: SettingFlow<Boolean> = SettingFlow(false)
    public val screenToUnlock: SettingFlow<NavigationRoute?> = SettingFlow(null)

    private val lockMethod: Flow<LockMethod> = PrivateSettingsStore.lockMethod.flow(application)

    init {
        viewModelInitialized()
    }


    public fun removeLock() {
        viewModelScope.launch {
            PrivateSettingsStore.lockPinHash.reset(application)
            PrivateSettingsStore.lockMethod.reset(application)
            unlock()
        }
    }

    public fun setPinLockMethod(pin: String) {
        viewModelScope.launch {
            val hash = securityService.hashPin(pin)
            PrivateSettingsStore.lockPinHash.set(application, hash)
            PrivateSettingsStore.lockMethod.set(application, LockMethod.Pin)
            application.showToast(application.getString(R.string.pin_set_success))
            unlock()
        }
    }

    public fun setLockScreenMethod() {
        viewModelScope.launch {
            PrivateSettingsStore.lockPinHash.reset(application)
            PrivateSettingsStore.lockMethod.set(application, LockMethod.Device)
            unlock()
        }
    }


    public fun lock() {
        logD(SECURITY_SERVICE) { "User asked to lock!" }
        isLocked.value = true
    }

    public fun unlock() {
        logD(SECURITY_SERVICE) { "User asked to unlock!" }
        isLocked.value = false
        screenToUnlock.value = null
    }

    public fun cancelUnlock() {
        screenToUnlock.value = null
    }

    public fun onEnterNewRoute(route: NavKey) {
        if (route !in NavigationRoute.settingsRoutes) {
            lock()
        }
    }


    public fun requestUnlock(targetScreen: NavigationRoute) {
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

    public fun isDeviceUnlockAvailable(): Boolean = securityService.isDeviceUnlockAvailable(application)

    public fun showDeviceUnlockPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ): Unit = securityService.showDeviceUnlockPrompt(
        activity = activity,
        onSuccess = onSuccess,
        onError = onError,
        onFailed = onFailed
    )

    public fun verifyPin(pin: String, storedHash: String): Boolean = securityService.verifyPin(pin, storedHash)
}
