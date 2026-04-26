package org.elnix.dragonlauncher.models.sinleton

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object AppsRepository {
    private val _reloadTrigger = Channel<Unit>(Channel.CONFLATED)
    val reloadTrigger = _reloadTrigger.receiveAsFlow()

    private var lastReloadTime = 0L
    private const val DEBOUNCE_MS = 500L

    fun triggerReload() {
        val now = System.currentTimeMillis()

        // Only trigger if enough time has passed since last reload
        if (now - lastReloadTime > DEBOUNCE_MS) {
            lastReloadTime = now
            _reloadTrigger.trySend(Unit)
        }
    }
}