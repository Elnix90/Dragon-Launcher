package org.elnix.dragonlauncher.profiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.os.UserManager.USER_TYPE_PROFILE_MANAGED
import android.os.UserManager.USER_TYPE_PROFILE_PRIVATE
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.elnix.dragonlauncher.PROFILES_TAG
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager

internal data class ProfileWithState(
    val profile: Profile,
    val state: Profile.State
)

public class ProfileManager(
    ctx: Context,
    private val permissionsManager: PermissionsManager
) {
    private val mutex = Mutex()

    private val userManager = ctx.getSystemService<UserManager>()!!
    private val launcherApps = ctx.getSystemService<LauncherApps>()!!

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    /**
     * An array of exactly 3 profiles with their states.
     * - Index 0: Personal profile
     * - Index 1: Work profile
     * - Index 2: Private profile
     *
     * Profiles that don't exist are null.
     */
    private val profileStates: MutableStateFlow<Array<ProfileWithState?>> =
        MutableStateFlow(arrayOf(null, null, null))

    /**
     * List of profiles that are active and unlocked.
     */
    public val activeProfiles: Flow<List<Profile>> =
        profileStates
            .map { states ->
                states.mapNotNull {
                    if (it?.state?.locked != false) null else it.profile
                }
            }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    public val profiles: Flow<List<Profile?>> =
        profileStates
            .map { states ->
                states.map { it?.profile }
            }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    init {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    scope.launch {
                        refreshProfiles()
                    }
                }
            }
        ctx.registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
                addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
                addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
                if (isAtLeastApiLevel(34)) {
                    addAction(Intent.ACTION_PROFILE_ADDED)
                    addAction(Intent.ACTION_PROFILE_REMOVED)
                }
                if (isAtLeastApiLevel(35)) {
                    addAction(Intent.ACTION_PROFILE_AVAILABLE)
                    addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
                }
                if (isAtLeastApiLevel(31)) {
                    addAction(Intent.ACTION_PROFILE_ACCESSIBLE)
                    addAction(Intent.ACTION_PROFILE_INACCESSIBLE)
                }
            }
        )
        scope.launch {
            if (isAtLeastApiLevel(35)) {
                permissionsManager.hasPermission(PermissionGroup.ManageProfiles).collectLatest {
                    refreshProfiles()
                }
            } else {
                refreshProfiles()
            }
        }
    }

    private suspend fun refreshProfiles() {
        mutex.withLock {
            val profiles = arrayOf<ProfileWithState?>(null, null, null)

            for (userHandle in launcherApps.profiles) {
                val serial = userManager.getSerialNumberForUser(userHandle)
                if (android.os.Build.MANUFACTURER == "samsung" && serial == 150L) continue // Hide Samsung Secure Folder

                val type = getProfileType(userHandle)
                val index =
                    when (type) {
                        Profile.Type.Personal -> 0
                        Profile.Type.Work -> 1
                        Profile.Type.Private -> 2
                    }

                if (profiles[index] == null) {
                    profiles[index] =
                        ProfileWithState(
                            Profile(
                                type = getProfileType(userHandle),
                                userHandle = userHandle,
                                serial = serial
                            ),
                            getProfileState(userHandle)
                        )
                }
            }
            profileStates.value = profiles
        }
    }

    public fun getProfile(userHandle: UserHandle): Flow<Profile?> =
        profileStates.map { profiles ->
            profiles.find { it?.profile?.userHandle == userHandle }?.profile
        }

    public fun getProfileState(profile: Profile?): Flow<Profile.State?> =
        profileStates.map { profiles ->
            profiles.find { it?.profile == profile }?.state
        }

    private fun getProfileType(userHandle: UserHandle): Profile.Type {
        if (isAtLeastApiLevel(35)) {
            val launcherUserInfo = launcherApps.getLauncherUserInfo(userHandle)
            return when (launcherUserInfo?.userType) {
                USER_TYPE_PROFILE_PRIVATE -> Profile.Type.Private
                USER_TYPE_PROFILE_MANAGED -> Profile.Type.Work
                else -> Profile.Type.Personal
            }
        }
        return if (userHandle == Process.myUserHandle()) Profile.Type.Personal else Profile.Type.Work
    }

    private fun getProfileState(userHandle: UserHandle): Profile.State {
        val quietMode =
            userHandle != Process.myUserHandle() &&
                userManager.isQuietModeEnabled(userHandle)
        val unlocked = userManager.isUserUnlocked(userHandle)
        return Profile.State(
            locked = quietMode || !unlocked
        )
    }

    /**
     * Whether the given profile is currently locked (quiet mode or not started).
     *
     * This is the source of truth used before deciding to request an unlock.
     * The running user is never considered locked, as `isQuietModeEnabled` is
     * meaningless for the main/personal user.
     */
    public fun isProfileLocked(profile: Profile): Boolean {
        if (profile.userHandle == Process.myUserHandle()) {
            return !userManager.isUserUnlocked(profile.userHandle)
        }
        return userManager.isQuietModeEnabled(profile.userHandle) ||
            !userManager.isUserUnlocked(profile.userHandle)
    }

    /**
     * Resolves a stored [Profile] (e.g. deserialized from a point action) to the
     * live profile currently present on this device, or null if it doesn't exist.
     *
     * The stored `userHandle` can be unreliable: it is serialized as a plain user
     * id and older persisted data may carry a wrong one. The `serial` is a stable,
     * device-wide unique identifier, so it is preferred when it matches.
     */
    public suspend fun resolveProfile(profile: Profile): Profile? {
        if (profileStates.value.none { it != null }) {
            runCatching { refreshProfiles() }
        }

        if (profile.serial != 0L) {
            profileStates.value
                .firstOrNull { it?.profile?.serial == profile.serial }
                ?.profile
                ?.let { return it }
        }

        return profileStates.value
            .firstOrNull { it?.profile?.userHandle == profile.userHandle }
            ?.profile
    }
//    public suspend fun resolveProfile(profile: Profile): Flow<Profile?> {
//        return profileStates.map { profiles ->
//            if (profiles.none { it != null }) {
//                runCatching { refreshProfiles() }
//            }
//
//            if (profile.serial != 0L) {
//                profiles.firstOrNull { it?.profile?.serial == profile.serial }
//                    ?.profile
//                    ?.let { return@map it }
//            }
//
//            profiles.firstOrNull { it?.profile?.userHandle == profile.userHandle }?.profile
//        }
//    }

    @RequiresApi(28)
    public fun unlockProfile(profile: Profile): Boolean =
        try {
            val requested = userManager.requestQuietModeEnabled(false, profile.userHandle)
            logI(PROFILES_TAG) { "Requested unlock of the ${profile.type} profile, success=$requested" }
            requested
        } catch (e: SecurityException) {
            logE(PROFILES_TAG, e) { "Security error while requesting unlock of the ${profile.type} profile" }
            false
        } catch (e: Exception) {
            logE(PROFILES_TAG, e) { "Failed to request unlock of the ${profile.type} profile" }
            false
        }

    @RequiresApi(28)
    public fun lockProfile(profile: Profile) {
        try {
            userManager.requestQuietModeEnabled(true, profile.userHandle)
            logI(PROFILES_TAG) { "Locked the ${profile.type} profile" }
        } catch (e: Exception) {
            logE(PROFILES_TAG, e) { "Failed to lock the ${profile.type} profile" }
        }
    }

    public fun isUserUnlocked(userHandle: UserHandle): Boolean = userManager.isUserUnlocked(userHandle)
}
