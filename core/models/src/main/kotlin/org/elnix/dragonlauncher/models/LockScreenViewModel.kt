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
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SECURITY_HELPER
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.messyfolder.SecurityHelper
import org.elnix.dragonlauncher.common.messyfolder.findFragmentActivity
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
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


    fun requestUnlock(targetScreen: NavigationRoute, onSuccess: () -> Unit) {
        when (_lockMethod.value) {
            LockMethod.NONE -> {
                unlock()
                onSuccess()
            }

            LockMethod.PIN -> {
                _screenToUnlock.value = targetScreen
            }

            LockMethod.DEVICE_UNLOCK -> {
                val activity = ctx.findFragmentActivity()
                if (activity != null && SecurityHelper.isDeviceUnlockAvailable(ctx)) {
                    SecurityHelper.showDeviceUnlockPrompt(
                        activity = activity,
                        onSuccess = {
                            unlock()
                            onSuccess()
                        },
                        onError = { msg ->
                            ctx.showToast(ctx.getString(org.elnix.dragonlauncher.common.R.string.authentication_error, msg))
                        },
                        onFailed = {
                            ctx.showToast(ctx.getString(org.elnix.dragonlauncher.common.R.string.authentication_failed))
                        }
                    )
                } else {
                    ctx.showToast(ctx.getString(R.string.device_credentials_not_available))
                }
            }
        }
    }
}
