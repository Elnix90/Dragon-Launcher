package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class PrivateSpaceViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    /** Tracks the private space unlocking requests */
    private val _privateSpaceUnlockRequest = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    val privateSpaceUnlockRequestEvents = _privateSpaceUnlockRequest.asSharedFlow()

    fun onUnlockPrivateSpace() {
        _privateSpaceUnlockRequest.tryEmit(Unit)
    }
}
