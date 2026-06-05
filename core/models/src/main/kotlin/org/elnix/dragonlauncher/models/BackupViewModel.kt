package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.models.utils.stateFlowDelegate
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val result by stateFlowDelegate<BackupResult?>(null)

    init {
        viewModelInitialized()
    }
}

data class BackupResult(
    val export: Boolean,
    val error: Boolean,
    val title: String,
    val message: String = ""
)
