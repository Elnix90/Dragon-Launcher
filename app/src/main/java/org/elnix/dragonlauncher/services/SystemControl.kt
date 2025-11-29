package org.elnix.dragonlauncher.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager

object SystemControl {

    fun isServiceEnabled(ctx: Context): Boolean {
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabled.contains(ctx.packageName)
    }

    fun openServiceSettings(ctx: Context) {
        ctx.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun getService(ctx: Context): SystemControlService? {
        val manager = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = manager.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        return enabled.find {
            it.resolveInfo.serviceInfo.packageName == ctx.packageName &&
                    it.resolveInfo.serviceInfo.name.contains("SystemControlService")
        }?.let { SystemControlService.INSTANCE }
    }

    /**
     * Called by SystemControlService.onCreate() to store a static instance.
     */
    fun attachInstance(service: SystemControlService) {
        SystemControlService.INSTANCE = service
    }

    @SuppressLint("WrongConstant", "PrivateApi")
    fun expandNotifications(ctx: Context) {
//        try {
//            val statusBarService = ctx.getSystemService("statusbar")
//            if (statusBarService != null) {
//                val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
//                val method = statusBarManagerClass.getMethod("expandNotificationsPanel")
//                method.invoke(statusBarService)
//                return
//            }
//        } catch (e: Exception) {
//            Log.e("SystemControl", "Reflection failed", e)
//        }

        // Fallback: Accessibility intent (no permissions needed)
//        try {
//            val intent = Intent("android.settings.NOTIFICATION_SHADE").apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            ctx.startActivity(intent)
//        } catch (e: Exception) {
//            Log.e("SystemControl", "Intent fallback failed", e)
//        }
        SystemControlService.INSTANCE?.openNotificationShade()
    }


    @SuppressLint("WrongConstant", "PrivateApi")
    fun expandQuickSettings(ctx: Context) {
        try {
            val statusBarService = ctx.getSystemService("statusbar")
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val method = statusBarManagerClass.getMethod("expandSettingsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            Log.e("SystemControl", "Reflection failed", e)
            // Fallback to notifications if quick settings fails
//            expandNotifications(ctx)
        }
    }
}
