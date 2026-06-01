package org.elnix.dragonlauncher.models

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.permissions.PermissionsManager
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    val permissionsManager: PermissionsManager
) : ViewModel() {

    init {
        viewModelInitialized()
    }
}
