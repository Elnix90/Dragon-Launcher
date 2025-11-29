package org.elnix.dragonlauncher.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent

@SuppressLint("AccessibilityPolicy")
class SystemControlService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
            flags =
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
    }

    fun openNotificationShade() {
        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

//    fun openQuickSettings() {
//        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
//    }

    companion object {
        var INSTANCE: SystemControlService? = null
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}
