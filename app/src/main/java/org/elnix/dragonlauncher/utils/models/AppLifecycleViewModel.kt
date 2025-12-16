package org.elnix.dragonlauncher.utils.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class AppLifecycleViewModel(application: Application) : AndroidViewModel(application) {
    private val _lastInteraction = MutableStateFlow(System.currentTimeMillis().toDouble())

//    val lastInteraction = _lastInteraction.asStateFlow()

    // Update the value, to ba able to compute on return
    fun onPause() {
        _lastInteraction.value = System.currentTimeMillis().toDouble()
    }


    // Return true if the time elapsed is inferior to the delta provided (if it can stay on the screen)
    fun resume(deltaMillis: Long): Boolean {
        val now = System.currentTimeMillis().toDouble()
        val last = _lastInteraction.value
        val elapsed = now - last
        _lastInteraction.value = now
        return elapsed > deltaMillis
    }
}
