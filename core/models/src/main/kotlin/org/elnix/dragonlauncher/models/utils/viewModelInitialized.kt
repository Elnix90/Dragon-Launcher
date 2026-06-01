package org.elnix.dragonlauncher.models.utils

import androidx.lifecycle.ViewModel
import org.elnix.dragonlauncher.logging.TAG
import org.elnix.dragonlauncher.logging.logD

internal fun ViewModel.viewModelInitialized() {
    logD(TAG) { "created ${this::javaClass.name} ${System.identityHashCode(this)}" }
}