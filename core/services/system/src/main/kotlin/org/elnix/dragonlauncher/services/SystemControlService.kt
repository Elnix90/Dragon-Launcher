package org.elnix.dragonlauncher.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import io.github.elnix90.logging.ACCESSIBILITY_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logW
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore

@SuppressLint("AccessibilityPolicy")
class SystemControlService : AccessibilityService() {


//    private var lastForegroundPackage: String? = null

    private var systemLauncher: String? = null
    private var autoRaiseEnabled = false

    // Service scope for Flows
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

//        val currentPkg = event?.packageName?.toString()

        if (!autoRaiseEnabled || systemLauncher == null) return

        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
//
//        if (currentPkg != null && currentPkg != systemLauncher) {
//            lastForegroundPackage = currentPkg
//        }

        val pkg = event.packageName?.toString() ?: return
        if (pkg != systemLauncher) {
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastLaunchTime < DEBOUNCE_DELAY_MS) return
        if (isSwitching.value) return  // Prevent recursive launch


//        val root = rootInActiveWindow ?: return

//        val ctx = this

//        if (isLikelyRecents(ctx, root)) {
//            logD(ACCESSIBILITY_TAG, "Blocked: structural recents screen")
//            return
//        }


        // This is so hacky lol, but it works (tested on 2 phones + 1 emulator)
        val eventText = event.text.joinToString(" ").lowercase()

        if (
            eventText.contains("recent") ||
            eventText.contains("overview") ||
            eventText.contains("apps")
        ) {
            logD(ACCESSIBILITY_TAG) { "Blocked recents by event text: $eventText" }
            return
        }


        // Doesn't work

//        val className = event.className?.toString() ?: ""
//        logW(ACCESSIBILITY_TAG, "──────────────────────────")
//        logW(ACCESSIBILITY_TAG, event.toString())
//        logW(ACCESSIBILITY_TAG, "className: $className")
//
//        val isRecentsScreen = className.contains("Recents") ||
//                className.contains("Overview") ||
//                className.contains("Task") ||
//                className.contains("RecentApps") ||
//                className.contains("MultiWindow") ||
//                className.contains("SplitScreen") ||
//                className.contains("ListView")
//
//
//        if (isRecentsScreen) {
//            logD(ACCESSIBILITY_TAG, "Blocked recents screen: $className")
//            return
//        }

//        val isMainHome = when {
//            className.contains("Launcher") -> true
//            className.contains("Home") && !className.contains("Screen") -> true
//            className.contains("Desktop") -> true
//            className.contains("Workspace") -> true
//            else -> {
//                val eventText = event.text.joinToString()
//                eventText.contains("Home", ignoreCase = true) ||
//                        eventText.contains("Desktop", ignoreCase = true)
//            }
//        }

//        if (
//            isMainHome &&
//            lastForegroundPackage != null &&
//            lastForegroundPackage != systemLauncher
//        ) {
        logD(ACCESSIBILITY_TAG) { "MAIN HOME SCREEN DETECTED, LAUNCHING DRAGON" }
        launchDragon()
//        } else {
//            logD(ACCESSIBILITY_TAG, "Skipped (not home): $className")
//        }
    }

    override fun onInterrupt() {
        logW(ACCESSIBILITY_TAG) { "Accessibility service interrupted" }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        listenToSettingsChanges()

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED

            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            notificationTimeout = 50
        }
        serviceInfo = info

        SystemControl.attachInstance(this)
        logD(ACCESSIBILITY_TAG) { "Service ready - Gestures & window monitoring enabled" }
    }

    fun openNotificationShade() {
        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

//    fun openQuickSettings() {
//        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
//    }

    fun openRecentApps() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }


    private fun launchDragon() {
        isSwitching.value = true
        lastLaunchTime = System.currentTimeMillis()

        SystemControl.launchDragon(this)
        logD(ACCESSIBILITY_TAG) { "Dragon Launcher launched" }

        // Post to handler for debounce; after launching Dragon, for faster visual effect
        Handler(Looper.getMainLooper()).postDelayed({
            isSwitching.value = false
        }, 100L)
    }

    private fun listenToSettingsChanges() {
        serviceScope.launch {
            val pkg = DebugSettingsStore.systemLauncherPackageName.get(this@SystemControlService)
            systemLauncher = pkg.ifBlank { null }
            logD(ACCESSIBILITY_TAG) { "Launcher setting updated: $pkg" }

        }

        serviceScope.launch {
            val enabled =
                DebugSettingsStore.autoRaiseDragonOnSystemLauncher.get(this@SystemControlService)
            autoRaiseEnabled = enabled
            logD(ACCESSIBILITY_TAG) { "Auto-raise toggled: $enabled" }

        }
    }

    companion object {
        var INSTANCE: SystemControlService? = null
        private const val DEBOUNCE_DELAY_MS = 500L
        private var lastLaunchTime = 0L
        private val isSwitching = mutableStateOf(false)
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
