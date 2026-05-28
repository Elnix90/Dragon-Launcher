@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import org.elnix.dragonlauncher.base.profiles.Profile
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.logging.TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.profiles.ProfileManager
import javax.inject.Inject

@HiltViewModel
class ProfilesVM @Inject constructor(
    application: Application,
    private val profileManager: ProfileManager,
    private val permissionsManager: PermissionsManager
) : AndroidViewModel(application) {

    val profiles = profileManager.profiles.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        replay = 1
    )
    val profileStates = profiles.flatMapLatest { profiles ->
        combine(profiles.map { profileManager.getProfileState(it) }) {
            it.toList()
        }
    }


    fun setProfileLock(profile: Profile?, locked: Boolean) {
        if (isAtLeastApiLevel(28) && profile != null) {
            if (locked) {
                profileManager.lockProfile(profile)
            } else {
                profileManager.unlockProfile(profile)
            }
        }
    }

    val hasProfilesPermission = permissionsManager.hasPermission(PermissionGroup.ManageProfiles)


    init {
        logD(TAG) { "created PrivateSpaceVM ${System.identityHashCode(this)}" }
    }
}
