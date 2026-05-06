package org.elnix.dragonlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BROADCAST_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.models.sinleton.AppsRepository

class PackageReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (
            action == Intent.ACTION_PACKAGE_ADDED ||
            action == Intent.ACTION_PACKAGE_REMOVED ||
            action == Intent.ACTION_PACKAGE_REPLACED ||
            action == Intent.ACTION_PACKAGES_SUSPENDED ||
            action == Intent.ACTION_PACKAGES_UNSUSPENDED ||
            action == Intent.ACTION_PACKAGE_CHANGED
        ) {
            val packageName = intent.data?.schemeSpecificPart

            logI(BROADCAST_TAG) { "Got intent: $intent, action: $action, pkg: $packageName" }

            try {
                AppsRepository.triggerReload()
            } catch (e: Exception) {
                logE(BROADCAST_TAG, e) { e.toString() }
            }
        }
    }
}
