package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.logD
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.TAG
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val HOME_REENTER_WINDOW_MS = 80L
private const val BLOCK_DELAY = 100L

@Stable
@HiltViewModel
public class AppLifecycleViewModel
    @Inject
    constructor(
        application: Application
    ) : AndroidViewModel(application) {
        init {
            viewModelInitialized()
        }

        /**  Tracks the home events */
        private val _homeEvents = Channel<Unit>(Channel.CONFLATED)
        public val homeEvents: Flow<Unit> = _homeEvents.receiveAsFlow()

        /** Computes when the app goes background, to return main screen after cooldown */
        public val lastInteraction: SettingFlow<Long> = SettingFlow(System.currentTimeMillis())

        private var homeActionBlocked = false

        public fun onHomeAction() {
            val now = System.currentTimeMillis()
            val delta = now - lastInteraction.value

            logD(TAG) { "Home intent delta: $delta (now=$now, last=${lastInteraction.value})" }

            if (homeActionBlocked) {
                logD(TAG) { "HOME intent blocked by homeActionBlocked" }
                return
            }

            // HOME pressed while launcher already visible
            if (delta in 0..HOME_REENTER_WINDOW_MS) {
                logD(TAG) { "HOME intent validated, sending to collector!" }
                _homeEvents.trySend(Unit)
            } else {
                logD(TAG) { "HOME intent discarded, fired too lately" }
            }
        }

        /**
         * Update the value, to be able to compute on return
         * */
        public fun updateLastInteraction() {
            lastInteraction.value = System.currentTimeMillis()
        }

        /**
         * Block home actions temporarily for [BLOCK_DELAY]ms, to prevent them to fire,
         * when user returns to launcher right after launching an action, such as launching an app
         */
        public fun blockHomeActionsTemporarily() {
            logD(TAG) { "Home action blocked for ${BLOCK_DELAY}ms" }
            homeActionBlocked = true
            viewModelScope.launch {
                delay(BLOCK_DELAY.milliseconds)
                homeActionBlocked = false
            }
        }

        /** Return true if the time elapsed is inferior to the delta provided (if it can stay on the screen) */
        public fun isTimeoutExceeded(timeoutSeconds: Long): Boolean {
            val now = System.currentTimeMillis()
            val last = lastInteraction.value
            val elapsed = now - last
            return elapsed > timeoutSeconds * 1000
        }
    }
