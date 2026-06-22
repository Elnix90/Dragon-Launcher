package org.elnix.dragonlauncher.models.utils

import androidx.lifecycle.ViewModel
import io.github.elnix90.logging.TAG
import io.github.elnix90.logging.logD

internal fun ViewModel.viewModelInitialized() {
    logD(TAG) { "Created viewModel '${this::class.java.name.substringAfterLast('.')}' ${System.identityHashCode(this)}" }
}