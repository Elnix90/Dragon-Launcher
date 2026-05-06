package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Settings.HOME_REENTER_WINDOW_MS
import org.elnix.dragonlauncher.logging.logD
import javax.inject.Inject

@HiltViewModel
class AppLifecycleViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {


    /**  Tracks the home events */
    private val _homeEvents = Channel<Unit>(Channel.CONFLATED)
    val homeEvents = _homeEvents.receiveAsFlow()


    /** Computes when the app goes background, to return main screen after cooldown */
    private val _lastInteraction = MutableStateFlow(System.currentTimeMillis())
    val lastInteraction = _lastInteraction.asStateFlow()

    fun onHomeAction() {
        val now = System.currentTimeMillis()
        val delta = now - _lastInteraction.value

        logD(TAG) { "Home intent delta: $delta (now=$now, last=${_lastInteraction.value})" }

        if (delta in 1..HOME_REENTER_WINDOW_MS) {
            // HOME pressed while launcher already visible

            logD(TAG) { "HOME intent validated, sending to collector!" }
            _homeEvents.trySend(Unit)
        }
    }

    // Update the value, to ba able to compute on return
    fun onPause() {
        _lastInteraction.value = System.currentTimeMillis()
    }


    /** Return true if the time elapsed is inferior to the delta provided (if it can stay on the screen) */
    fun isTimeoutExceeded(timeoutSeconds: Long): Boolean {
        val now = System.currentTimeMillis()
        val last = _lastInteraction.value
        val elapsed = now - last
        _lastInteraction.value = now
        return elapsed > timeoutSeconds * 1000
    }
}
