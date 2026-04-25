package org.elnix.dragonlauncher.reveiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.common.utils.Constants.Logging.BROADCAST_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.TAG
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import javax.inject.Inject

@AndroidEntryPoint
class PackageReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appsViewModel: AppsViewModel
    companion object {
        const val CHANNEL_IPS = "ips_regeneration"
        const val NOTIF_ID_IPS = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        logI(BROADCAST_TAG) { "Got intent: $intent" }

        if (
            action == Intent.ACTION_PACKAGE_ADDED ||
            action == Intent.ACTION_PACKAGE_REMOVED ||
            action == Intent.ACTION_PACKAGE_REPLACED ||
            action == Intent.ACTION_PACKAGES_SUSPENDED ||
            action == Intent.ACTION_PACKAGES_UNSUSPENDED ||
            action == Intent.ACTION_PACKAGE_CHANGED
        ) {
            val packageName = intent.data?.schemeSpecificPart
            val scope = CoroutineScope(Dispatchers.Default)

            logI(BROADCAST_TAG) { "Got intent: $intent, action! $action, pkg: $packageName" }
            if (packageName != context.packageName) {
                try {
                    scope.launch {
                        appsViewModel.reloadApps()

                        // If a new app is added and the user uses an IPS exported icon pack,
                        // suggest regenerating the pack in Icon Pack Studio.
                        // We skip this for updates/replacements (ACTION_PACKAGE_ADDED with EXTRA_REPLACING).
                        val isUpdate = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                        if (action == Intent.ACTION_PACKAGE_ADDED && !isUpdate) {
                            val selectedPack = UiSettingsStore.selectedIconPack.get(context)
                            if (selectedPack == "ginlemon.iconpackstudio.exported") {
                                notifyIpsRegeneration(context)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logE(TAG, e) { e.toString() }
                }
            }
        }
    }

    private fun notifyIpsRegeneration(ctx: Context) {
        try {
            val ipsIntent = ctx.packageManager.getLaunchIntentForPackage("ginlemon.iconpackstudio")
            if (ipsIntent != null) {
                ipsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Create channel for Android O+
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_IPS,
                        "Icon Pack Studio",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Notifications to refresh your Icon Pack Studio icons"
                    }
                )

                val pendingIntent = PendingIntent.getActivity(
                    ctx, 0, ipsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(ctx, CHANNEL_IPS)
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentTitle("New app installed")
                    .setContentText("Refresh your Icon Pack Studio icons to include this app.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .addAction(
                        R.mipmap.ic_launcher_foreground,
                        "Open Icon Pack Studio",
                        pendingIntent
                    )
                    .build()

                nm.notify(NOTIF_ID_IPS, notification)
            }
        } catch (e: Exception) {
            logE(TAG, e) { "Failed to suggest IPS regeneration" }
        }
    }
}
