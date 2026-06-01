package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.logging.TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

private const val HOME_REENTER_WINDOW_MS = 80L
private const val BLOCK_DELAY = 100L


@HiltViewModel
class AppLifecycleViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {


    init {
        viewModelInitialized()
    }

    /**  Tracks the home events */
    private val _homeEvents = Channel<Unit>(Channel.CONFLATED)
    val homeEvents = _homeEvents.receiveAsFlow()


    /** Computes when the app goes background, to return main screen after cooldown */
    private val _lastInteraction = MutableStateFlow(System.currentTimeMillis())
    val lastInteraction = _lastInteraction.asStateFlow()

    private var homeActionBlocked = false

    fun onHomeAction() {
        val now = System.currentTimeMillis()
        val delta = now - _lastInteraction.value

        logD(TAG) { "Home intent delta: $delta (now=$now, last=${_lastInteraction.value})" }

        if (homeActionBlocked) {
            logD(TAG) { "HOME intent blocked by homeActionBlocked" }
            return
        }

        // HOME pressed while launcher already visible
        if (delta in 1..HOME_REENTER_WINDOW_MS) {
            logD(TAG) { "HOME intent validated, sending to collector!" }
            _homeEvents.trySend(Unit)
        } else {
            logD(TAG) { "HOME intent discarded, fired too lately" }
        }
    }

    /**
     * Update the value, to be able to compute on return
     * */
    fun updateLastInteraction() {
        logD(TAG) { "Last interaction updated!" }
        _lastInteraction.value = System.currentTimeMillis()
    }

    /**
     * Block home actions temporarily for [HOME_REENTER_WINDOW_MS]ms, to prevent them to fire,
     * when user returns to launcher right after launching an action, such as launching an app
     */
    fun blockHomeActionsTemporarily() {
        logD(TAG) { "Home action blocked for ${BLOCK_DELAY}ms" }
        homeActionBlocked = true
        viewModelScope.launch {
            delay(BLOCK_DELAY)
            homeActionBlocked = false
        }
    }

    /** Return true if the time elapsed is inferior to the delta provided (if it can stay on the screen) */
    fun isTimeoutExceeded(timeoutSeconds: Long): Boolean {
        val now = System.currentTimeMillis()
        val last = _lastInteraction.value
        val elapsed = now - last
        return elapsed > timeoutSeconds * 1000
    }
}
