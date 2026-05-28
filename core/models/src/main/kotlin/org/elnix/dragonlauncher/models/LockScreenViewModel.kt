package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.messyfolder.SecurityHelper
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.logging.SECURITY_HELPER
import org.elnix.dragonlauncher.logging.TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import javax.inject.Inject

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val _lockMethod = MutableStateFlow(LockMethod.NONE)
    val lockMethod = _lockMethod.asStateFlow()


    private val _isLocked = MutableStateFlow(true)
    val isLocked = _isLocked.asStateFlow()

    private val _screenToUnlock = MutableStateFlow<NavigationRoute?>(null)
    val screenToUnlock = _screenToUnlock.asStateFlow()


    init {
        loadLockMethod()
        logD(TAG) { "created LockScreenVM ${System.identityHashCode(this)}" }
    }

    private fun loadLockMethod() {
        viewModelScope.launch {
            _lockMethod.value = PrivateSettingsStore.lockMethod.get(ctx)
        }
    }

    fun removeLock() {
        viewModelScope.launch {
            PrivateSettingsStore.lockPinHash.reset(ctx)
            PrivateSettingsStore.lockMethod.reset(ctx)
            unlock()
        }
    }

    fun setPinLockMethod(pin: String) {
        viewModelScope.launch{
            val hash = SecurityHelper.hashPin(pin)
            PrivateSettingsStore.lockPinHash.set(ctx, hash)
            PrivateSettingsStore.lockMethod.set(ctx, LockMethod.PIN)
            ctx.showToast(ctx.getString(R.string.pin_set_success))
            unlock()
        }
    }

    fun setLockScreenMethod() {
        viewModelScope.launch{
            PrivateSettingsStore.lockPinHash.reset(ctx)
            PrivateSettingsStore.lockMethod.set(ctx, LockMethod.DEVICE_UNLOCK)
            unlock()
        }
    }


    fun lock() {
        logD(SECURITY_HELPER) { "User asked to lock!" }
        _isLocked.value = true
    }

    fun unlock() {
        logD(SECURITY_HELPER) { "User asked to unlock!" }
        _isLocked.value = false
        _screenToUnlock.value = null
    }

    fun cancelPinUnlock() {
        _screenToUnlock.value = null
    }

    fun onEnterNewRoute(route: NavKey) {
        if (route !in NavigationRoute.settingsRoutes) {
            lock()
        }
    }




    fun requestUnlock(targetScreen: NavigationRoute) {
        when (_lockMethod.value) {
            LockMethod.NONE -> unlock()

            LockMethod.PIN -> {
                _screenToUnlock.value = targetScreen
            }

            LockMethod.DEVICE_UNLOCK -> {
                _screenToUnlock.value = targetScreen
            }
        }
    }
}
