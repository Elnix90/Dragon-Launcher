package org.elnix.dragonlauncher.models.utils

import androidx.lifecycle.ViewModel
import io.github.elnix90.logging.logD
import org.elnix.dragonlauncher.TAG

internal fun ViewModel.viewModelInitialized() {
    logD(TAG) {
        "Created viewModel '${this::class.java.name.substringAfterLast('.')}' ${System.identityHashCode(this)}"
    }
}
