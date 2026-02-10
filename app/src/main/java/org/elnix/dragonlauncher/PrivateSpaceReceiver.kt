package org.elnix.dragonlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.logging.logD
import org.elnix.dragonlauncher.common.logging.logE
import org.elnix.dragonlauncher.common.logging.logI

/**
 * BroadcastReceiver to listen for Private Space lock/unlock events (Android 15+).
 * 
 * Listens to:
 * - ACTION_PROFILE_AVAILABLE: Private Space is unlocked and accessible
 * - ACTION_PROFILE_UNAVAILABLE: Private Space is locked
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class PrivateSpaceReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        // Guard against running on devices below Android 15 (Vanilla Ice Cream)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            logD("PrivateSpaceReceiver", "Ignoring broadcast on unsupported API level: ${Build.VERSION.SDK_INT}")
            return
        }
        
        val action = intent.action ?: return
        val userHandle = intent.getParcelableExtra<UserHandle>(Intent.EXTRA_USER)
        
        logD("PrivateSpaceReceiver", "Received action: $action for user: $userHandle")
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (action) {
                    Intent.ACTION_PROFILE_AVAILABLE -> {
                        // Private Space is now unlocked
                        logD("PrivateSpaceReceiver", "Private Space unlocked - reloading apps")
                        handlePrivateSpaceUnlocked(context, userHandle)
                    }
                    Intent.ACTION_PROFILE_UNAVAILABLE -> {
                        // Private Space is now locked
                        logD("PrivateSpaceReceiver", "Private Space locked - reloading apps")
                        handlePrivateSpaceLocked(context, userHandle)
                    }
                    else -> {
                        logD("PrivateSpaceReceiver", "Unknown action: $action")
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private suspend fun handlePrivateSpaceUnlocked(context: Context, userHandle: UserHandle?) {
        logD("PrivateSpaceReceiver", "handlePrivateSpaceUnlocked called")
        // Reload apps to include Private Space apps
        try {
            val app = context.applicationContext as? MyApplication
            if (app == null) {
                logD("PrivateSpaceReceiver", "ERROR: Could not get MyApplication instance")
                return
            }
            
            logD("PrivateSpaceReceiver", "Calling appsViewModel.reloadApps()...")
            app.appsViewModel.reloadApps()
            logD("PrivateSpaceReceiver", "Apps reloaded successfully after Private Space unlock")
        } catch (e: Exception) {
            logE("PrivateSpaceReceiver", "ERROR reloading apps: ${e.message}", e)
        }
    }
    
    private suspend fun handlePrivateSpaceLocked(context: Context, userHandle: UserHandle?) {
        logD("PrivateSpaceReceiver", "handlePrivateSpaceLocked called")
        // Reload apps to hide Private Space apps
        try {
            val app = context.applicationContext as? MyApplication
            if (app == null) {
                logD("PrivateSpaceReceiver", "ERROR: Could not get MyApplication instance")
                return
            }
            
            logD("PrivateSpaceReceiver", "Calling appsViewModel.reloadApps()...")
            app.appsViewModel.reloadApps()
            logD("PrivateSpaceReceiver", "Apps reloaded successfully after Private Space lock")
        } catch (e: Exception) {
            logE("PrivateSpaceReceiver", "ERROR reloading apps: ${e.message}", e)
        }
    }
}
