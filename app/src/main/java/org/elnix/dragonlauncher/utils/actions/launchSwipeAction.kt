package org.elnix.dragonlauncher.utils.actions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.services.SystemControl


/**
 * Exception for app launch failures
 */
class AppLaunchException(message: String, cause: Throwable? = null) : Exception(message, cause)


fun launchSwipeAction(ctx: Context, action: SwipeActionSerializable?, onAppDrawer: () -> Unit) {
    if (action == null) return

    when (action) {

        is SwipeActionSerializable.LaunchApp -> {
            val i = ctx.packageManager.getLaunchIntentForPackage(action.packageName)
            if (i != null) {
                try {
                    ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: SecurityException) {
                    throw AppLaunchException("Security error launching ${action.packageName}", e)
                } catch (e: NullPointerException) {
                    throw AppLaunchException("App component not found for ${action.packageName}", e)
                } catch (e: Exception) {
                    throw AppLaunchException("Failed to launch ${action.packageName}", e)
                }
            }
        }

        is SwipeActionSerializable.OpenUrl -> {
            val i = Intent(Intent.ACTION_VIEW, action.url.toUri())
            ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        SwipeActionSerializable.NotificationShade -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.expandNotifications(ctx)
        }

        SwipeActionSerializable.ControlPanel -> {
            SystemControl.expandQuickSettings(ctx)
        }

        SwipeActionSerializable.OpenAppDrawer -> {
            onAppDrawer()
        }
    }
}
