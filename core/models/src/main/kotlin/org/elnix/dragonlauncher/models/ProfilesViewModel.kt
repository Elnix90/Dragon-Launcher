@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.profiles.ProfileManager
import javax.inject.Inject

@HiltViewModel
public class ProfilesViewModel @Inject constructor(
    application: Application,
    private val profileManager: ProfileManager,
    private val permissionsManager: PermissionsManager
) : AndroidViewModel(application) {

    public val profiles: SharedFlow<List<Profile?>> = profileManager.profiles.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        replay = 1
    )

    public val profileStates: Flow<List<Profile.State?>> = profiles.flatMapLatest { profiles ->
        combine(profiles.map { profileManager.getProfileState(it) }) {
            it.toList()
        }
    }


    public fun askProfileLock(profile: Profile?, locked: Boolean) {
        if (isAtLeastApiLevel(28) && profile != null) {
            if (locked) {
                profileManager.lockProfile(profile)
            } else {
                profileManager.unlockProfile(profile)
            }
        }
    }

    public val hasProfilesPermission: Flow<Boolean> = permissionsManager.hasPermission(PermissionGroup.ManageProfiles)


    init {
        viewModelInitialized()
    }
}
