package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class AppLifecycleViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {


    /** ───────────── Tracks the home events ─────────────*/
    private val _homeEvents = Channel<Unit>(Channel.CONFLATED)
    val homeEvents = _homeEvents.receiveAsFlow()

    fun launchHomeAction() {
        _homeEvents.trySend(Unit)
    }


    /** ───────────── Tracks the private space unlocking requests ─────────────*/
    private val _privateSpaceUnlockRequest = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    val privateSpaceUnlockRequestEvents = _privateSpaceUnlockRequest.asSharedFlow()

    fun onUnlockPrivateSpace() {
        _privateSpaceUnlockRequest.tryEmit(Unit)
    }

    /** ───────────── Computes when the app goes background, to return main screen after cooldown (10 sec) ─────────────*/
    private val _lastInteraction = MutableStateFlow(System.currentTimeMillis().toDouble())

//    val lastInteraction = _lastInteraction.asStateFlow()

    // Update the value, to ba able to compute on return
    fun onPause() {
        _lastInteraction.value = System.currentTimeMillis().toDouble()
    }


    /** Return true if the time elapsed is inferior to the delta provided (if it can stay on the screen) */
    fun isTimeoutExceeded(timeoutSeconds: Int): Boolean {
        val now = System.currentTimeMillis().toDouble()
        val last = _lastInteraction.value
        val elapsed = now - last
        _lastInteraction.value = now
        return elapsed > timeoutSeconds * 1000
    }
}
