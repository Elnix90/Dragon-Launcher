package org.elnix.dragonlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.utils.TAG
import org.elnix.dragonlauncher.utils.logs.logE

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REMOVED) {
            val packageName = intent.data?.schemeSpecificPart
            val scope = CoroutineScope(Dispatchers.Default)
            if (packageName != context.packageName) {
                try {
                    val app = context.applicationContext as MyApplication
                    scope.launch {
                        app.appsViewModel.reloadApps()
                    }
                } catch (e: Exception) {
                    logE(TAG, e.toString())
                }
            }
        }
    }
}
