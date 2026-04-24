package org.elnix.dragonlauncher.reveiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.DragonLauncherApplication
import org.elnix.dragonlauncher.common.utils.Constants.Logging.PRIVATE_SPACE_TAG
import org.elnix.dragonlauncher.common.utils.PrivateSpaceUtils
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.models.AppsViewModel

/**
 * BroadcastReceiver to listen for Private Space lock/unlock events (Android 15+).
 *
 * Listens to:
 * - ACTION_PROFILE_AVAILABLE: Private Space is unlocked and accessible
 * - ACTION_PROFILE_UNAVAILABLE: Private Space is locked
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class PrivateSpaceReceiver(
    private val appsViewModel: AppsViewModel
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return

        if (action != Intent.ACTION_PROFILE_AVAILABLE && action != Intent.ACTION_PROFILE_UNAVAILABLE) {
            return
        }

        val pendingResult = goAsync()
        val app = context.applicationContext as? DragonLauncherApplication
        if (app == null) {
            pendingResult.finish()
            return
        }

        app.appScope.launch {
            try {
                val eventUser = intent.extractEventUserHandle()
                val privateUser = PrivateSpaceUtils.getPrivateSpaceUserHandle(context)

                if (privateUser == null || eventUser == null || eventUser != privateUser) {
                    logD(PRIVATE_SPACE_TAG) {
                        "Ignoring profile action=$action for non-private user=$eventUser"
                    }
                    return@launch
                }

                logI(PRIVATE_SPACE_TAG) { "Private Space action=$action, reloading apps" }
                appsViewModel.unlockAndReloadPrivateSpace()
            } catch (e: Exception) {
                logE(PRIVATE_SPACE_TAG, e) { "Failed to process Private Space broadcast" }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun Intent.extractEventUserHandle(): UserHandle? =
        getParcelableExtra(Intent.EXTRA_USER, UserHandle::class.java)
}
