package org.elnix.dragonlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.elnix.dragonlauncher.activity.MainActivity


/**
 * Used to force Android to launch dragon on reboot, making it registered by the system.
 * Sometimes on phones, after a reboot, the gestures are completely gone, and we can't preform the home action directly
 * without firstly changing the default launcher to the Default one, and then back to Dragon
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Just bring the launcher to foreground so Android registers it
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)
        }
    }
}