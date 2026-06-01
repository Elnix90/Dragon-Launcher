package org.elnix.dragonlauncher.common.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import org.elnix.dragonlauncher.logging.PRIVATE_SPACE_UTILS
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.logging.logW

/**
 * Utility functions for managing Android 15+ Private Space.
 */
object PrivateSpaceUtils {
    private const val PRIVATE_PROFILE_TYPE = "android.os.usertype.profile.PRIVATE"

    /**
     * Check if the device has Private Space capability (Android 15+)
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun isPrivateSpaceSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    }

    /**
     * Get the UserHandle for the Private Space profile, if it exists.
     * Returns null if Private Space is not available or not set up.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun getPrivateSpaceUserHandle(ctx: Context): UserHandle? {
        if (!isPrivateSpaceSupported()) return null

        try {
            val userManager = ctx.getSystemService(Context.USER_SERVICE) as UserManager
            val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return null

            val userProfiles = userManager.userProfiles

            for (userHandle in userProfiles) {
                if (userHandle == Process.myUserHandle()) continue

                try {
                    val userInfo = launcherApps.getLauncherUserInfo(userHandle)
                    if (userInfo?.userType == PRIVATE_PROFILE_TYPE) {
                        logD(PRIVATE_SPACE_UTILS) { "Found Private Space profile: $userHandle" }
                        return userHandle
                    }
                } catch (e: Exception) {
                    logE(PRIVATE_SPACE_UTILS, e) { "Error checking user profile" }
                }
            }

            logI(PRIVATE_SPACE_UTILS) { "No Private Space profile found" }
            return null
        } catch (e: Exception) {
            logE(PRIVATE_SPACE_UTILS, e) { "Error getting Private Space user handle" }
            return null
        }
    }

    /**
     * Check if Private Space is currently locked (quiet mode enabled).
     * Returns null if Private Space is not available.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun isPrivateSpaceLocked(ctx: Context): Boolean? {
        val privateUserHandle = getPrivateSpaceUserHandle(ctx) ?: return null

        try {
            val userManager = ctx.getSystemService(Context.USER_SERVICE) as UserManager
            val isLocked = userManager.isQuietModeEnabled(privateUserHandle)
            logD(PRIVATE_SPACE_UTILS) { "Private Space locked status: $isLocked" }
            return isLocked
        } catch (e: Exception) {
            logE(PRIVATE_SPACE_UTILS, e) { "Error checking Private Space lock status" }
            return null
        }
    }


    // SEEMS UNUSED -> COMMENTED
//    /**
//     * Open the Private Space settings/unlock UI.
//     * Returns an Intent to launch the Private Space settings where user can authenticate.
//     * The caller should launch this with startActivity() and then poll isPrivateSpaceLocked()
//     * to detect when authentication succeeds.
//     */
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    fun createPrivateSpaceSettingsIntent(): android.content.Intent {
//        // Open Private Space settings where user can unlock
//        val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
//        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
//        return intent
//    }

    /**
     * Request to unlock Private Space programmatically.
     * This may trigger authentication on some devices, but behavior varies by OEM.
     * Returns true if request was accepted (doesn't guarantee unlock succeeded).
     *
     * Recommended: After calling this, poll isPrivateSpaceLocked() to detect actual unlock.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun requestUnlockPrivateSpace(ctx: Context): Boolean {
        val privateUserHandle = getPrivateSpaceUserHandle(ctx) ?: run {
            logW(PRIVATE_SPACE_UTILS) { "Cannot unlock: Private Space not found" }
            return false
        }

        try {
            val userManager = ctx.getSystemService(Context.USER_SERVICE) as UserManager

            // Check if already unlocked
            if (!userManager.isQuietModeEnabled(privateUserHandle)) {
                logI(PRIVATE_SPACE_UTILS) { "Private Space is already unlocked" }
                return true
            }

            logI(PRIVATE_SPACE_UTILS) { "Requesting Private Space unlock" }

            // Request to disable quiet mode (unlock)
            // On some devices this may trigger biometric auth, on others it may do nothing
            val success = userManager.requestQuietModeEnabled(false, privateUserHandle)

            logI(PRIVATE_SPACE_UTILS) { "requestQuietModeEnabled returned: $success" }
            return success
        } catch (e: Exception) {
            logE(PRIVATE_SPACE_UTILS, e) { "Error requesting Private Space unlock" }
            return false
        }
    }

    // SEEMS UNUSED -> COMMENTED
//    /**
//     * Check if Private Space unlock is available/supported.
//     * This is a lighter check that doesn't require permissions.
//     */
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    fun canUnlockPrivateSpace(context: Context): Boolean {
//        return getPrivateSpaceUserHandle(context) != null
//    }
//
//    /**
//     * Request to lock the Private Space.
//     * Returns true if the request was successfully initiated.
//     */
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    fun requestLockPrivateSpace(context: Context): Boolean {
//        val privateUserHandle = getPrivateSpaceUserHandle(context) ?: run {
//            logE(PRIVATE_SPACE_UTILS, "Cannot lock: Private Space not found")
//            return false
//        }
//
//        try {
//            val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
//
//            // Check if already locked
//            if (userManager.isQuietModeEnabled(privateUserHandle)) {
//                logI(PRIVATE_SPACE_UTILS, "Private Space is already locked")
//                return true
//            }
//
//            logI(PRIVATE_SPACE_UTILS, "Requesting Private Space lock")
//
//            // Request to enable quiet mode (lock)
//            val success = userManager.requestQuietModeEnabled(true, privateUserHandle)
//
//            if (success) {
//                logI(PRIVATE_SPACE_UTILS, "Private Space locked successfully")
//            } else {
//                logE(PRIVATE_SPACE_UTILS, "Private Space lock request failed")
//            }
//
//            return success
//        } catch (e: Exception) {
//            logE(PRIVATE_SPACE_UTILS, "Error requesting Private Space lock", e)
//            return false
//        }
//    }
//
//    /**"
//     * Check if Private Space exists and has apps.
//     */
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    fun hasPrivateSpaceApps(context: Context): Boolean {
//        val privateUserHandle = getPrivateSpaceUserHandle(context) ?: return false
//
//        try {
//            val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return false
//            val activities = launcherApps.getActivityList(null, privateUserHandle)
//            return activities.isNotEmpty()
//        } catch (e: Exception) {
//            logE(PRIVATE_SPACE_UTILS, "Error checking Private Space apps")
//            return false
//        }
//    }
}
