package org.elnix.dragonlauncher.utils.actions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.utils.expandQuickActionsDrawer
import org.elnix.dragonlauncher.utils.hasUriPermission
import org.elnix.dragonlauncher.utils.showToast


/**
 * Exception for app launch failures
 */
class AppLaunchException(message: String, cause: Throwable? = null) : Exception(message, cause)


fun launchSwipeAction(
    ctx: Context,
    action: SwipeActionSerializable?,
    useAccessibilityInsteadOfContextToExpandActionPanel: Boolean = false,
    onAskWhatMethodToUseToOpenQuickActions: (() -> Unit)? = null,
    onReselectFile: (() -> Unit)? = null,
    onAppSettings: (() -> Unit)? = null,
    onAppDrawer: (() -> Unit)? = null
) {
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
                ctx.showToast("Please enable accessibility settings to use that feature")
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.expandNotifications(ctx)
        }

        SwipeActionSerializable.ControlPanel -> {
            if (useAccessibilityInsteadOfContextToExpandActionPanel) {
                SystemControl.expandQuickSettings(
                    ctx
                )
            }
            else expandQuickActionsDrawer(ctx)
            onAskWhatMethodToUseToOpenQuickActions?.invoke()

        }

        SwipeActionSerializable.OpenAppDrawer -> {
            onAppDrawer?.invoke()
        }

        SwipeActionSerializable.OpenDragonLauncherSettings -> onAppSettings?.invoke()

        SwipeActionSerializable.Lock -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                ctx.showToast("Please enable accessibility settings to use that feature")
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.lockScreen(ctx)
        }

        is SwipeActionSerializable.OpenFile -> {
            try {
                val uri = action.uri.toUri()

                if (!ctx.hasUriPermission(uri)) {
                    ctx.showToast("Please reselect the file to allow access")
                    onReselectFile?.invoke()
                    return
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, action.mimeType ?: "*/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                if (intent.resolveActivity(ctx.packageManager) != null) {
                    ctx.startActivity(intent)
                } else {
                    ctx.showToast("No app available to open this file")
                }

            } catch (e: Exception) {
                ctx.showToast("Unable to open file: ${e.message}")
//                Log.e("OpenFile", e.toString())
            }
        }
    }
}
