package org.elnix.dragonlauncher.activity

import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.PINNED_SHORTCUTS
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import java.util.UUID

/**
 * Activity that receives pinned shortcut requests from other apps.
 *
 * When an app calls [android.content.pm.ShortcutManager.requestPinShortcut],
 * the system forwards the request to the default launcher. This activity handles
 * that request, accepts the shortcut, and adds it as a new swipe point on the
 * first circle (circle 0, nest 0).
 *
 * The Android system sends an intent with action
 * [LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT] to this activity.
 */
class PinnedShortcutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcherApps = getSystemService(LauncherApps::class.java)
        if (launcherApps == null) {
            logW(PINNED_SHORTCUTS) { "LauncherApps service not available" }
            finish()
            return
        }

        val request = launcherApps.getPinItemRequest(intent)
        if (request == null) {
            logW(PINNED_SHORTCUTS) { "No pin item request found in intent" }
            finish()
            return
        }

        val shortcutInfo = request.shortcutInfo
        if (shortcutInfo == null) {
            logW(PINNED_SHORTCUTS) { "Pin request does not contain a shortcut (maybe a widget?)" }
            // For now, we don't handle widget pin requests
            finish()
            return
        }

        val packageName = shortcutInfo.`package`
        val shortcutId = shortcutInfo.id
        val shortLabel = shortcutInfo.shortLabel?.toString() ?: shortcutId

        logD(PINNED_SHORTCUTS) { "Received pin request: $packageName / $shortcutId ($shortLabel)" }

        // Accept the pin request — this tells the system the shortcut is pinned
        val accepted = request.accept()
        if (!accepted) {
            logW(PINNED_SHORTCUTS) { "Failed to accept pin request for $packageName / $shortcutId" }
            showToast(getString(R.string.pinned_shortcut_failed))
            finish()
            return
        }

        logD(PINNED_SHORTCUTS) { "Pin request accepted for $packageName / $shortcutId" }

        // Add the shortcut as a new swipe point on circle 0, nest 0
        lifecycleScope.launch {
            try {
                val existingPoints = SwipeSettingsStore.getPoints(this@PinnedShortcutActivity)

                // Check if this shortcut is already added
                val alreadyExists = existingPoints.any { point ->
                    val action = point.action
                    action is SwipeActionSerializable.LaunchShortcut &&
                            action.packageName == packageName &&
                            action.shortcutId == shortcutId
                }

                if (alreadyExists) {
                    showToast(
                        getString(
                            R.string.pinned_shortcut_already_exists,
                            shortLabel
                        )
                    )
                    finish()
                    return@launch
                }

                // Create a new point at a random angle on circle 0
                val angle = (0..359).random().toDouble()

                val newPoint = SwipePointSerializable(
                    id = UUID.randomUUID().toString(),
                    angleDeg = angle,
                    action = SwipeActionSerializable.LaunchShortcut(packageName, shortcutId),
                    circleNumber = 0,
                    nestId = 0
                )

                // Save the updated points list
                val updatedPoints = existingPoints + newPoint
                SwipeSettingsStore.savePoints(this@PinnedShortcutActivity, updatedPoints)

                logD(PINNED_SHORTCUTS) { "Shortcut added as point: $shortLabel at $angle°" }
                showToast(
                    getString(
                        R.string.pinned_shortcut_added,
                        shortLabel
                    )
                )
            } catch (e: Exception) {
                logE(PINNED_SHORTCUTS, e) { "Failed to save pinned shortcut" }
                showToast(getString(R.string.pinned_shortcut_failed))
            }

            finish()
        }
    }
}
