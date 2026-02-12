package org.elnix.dragonlauncher.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import org.elnix.dragonlauncher.common.logging.logD
import org.elnix.dragonlauncher.common.logging.logE
import org.elnix.dragonlauncher.common.utils.Constants.Logging.ACCESSIBILITY_TAG
import org.elnix.dragonlauncher.common.utils.showToast

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
            AccessibilityServiceInfo.FEEDBACK_GENERIC
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

//    @SuppressLint("WrongConstant", "PrivateApi")
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
//            this.logE(ACCESSIBILITY_TAG, "Reflection failed", e)
//        }

        // Fallback: Accessibility intent (no permissions needed)
//        try {
//            val intent = Intent("android.settings.NOTIFICATION_SHADE").apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            ctx.startActivity(intent)
//        } catch (e: Exception) {
//            this.logE(ACCESSIBILITY_TAG, "Intent fallback failed", e)
//        }
        SystemControlService.INSTANCE?.openNotificationShade()
    }


//    @SuppressLint("WrongConstant", "PrivateApi")
    fun expandQuickSettings(ctx: Context) {
        try {
            val statusBarService = ctx.getSystemService("statusbar")
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val method = statusBarManagerClass.getMethod("expandSettingsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            this.logE(ACCESSIBILITY_TAG, "Reflection failed", e)
            // Fallback to notifications if quick settings fails
            expandNotifications(ctx)
        }
    }


    fun lockScreen(ctx: Context) {
        if (!isServiceEnabled(ctx)) {
            openServiceSettings(ctx)
            return
        }
        SystemControlService.INSTANCE?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
        )
    }

    fun openRecentApps(ctx: Context) {
        if (!isServiceEnabled(ctx)) {
            ctx.showToast("Please enable accessibility settings to use that feature")
            openServiceSettings(ctx)
            return
        }

        logD(ACCESSIBILITY_TAG, "Got order to open recents")
        SystemControlService.INSTANCE?.openRecentApps()
    }

    fun isDeviceAdminActive(ctx: Context): Boolean {
        val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = ComponentName(ctx, DeviceAdminReceiver::class.java)
        return dpm.isAdminActive(component)
    }

    fun openDeviceAdminSettings(ctx: Context) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            val component = ComponentName(ctx, DeviceAdminReceiver::class.java)
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Prevent app kills & ensure persistence")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }

    fun activateDeviceAdmin(ctx: Context) {
        if (isDeviceAdminActive(ctx)) {
            ctx.showToast("Device Admin already active")
            return
        }

        val componentName = ComponentName(ctx, org.elnix.dragonlauncher.services.DeviceAdminReceiver::class.java)
        logD(ACCESSIBILITY_TAG, "component name: $componentName")


        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Required for persistence on Xiaomi - prevents battery kills")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        logD(ACCESSIBILITY_TAG, "intent: $intent")


        // Verify component exists (common APK issue)
        val adminReceiver = ctx.packageManager.getReceiverInfo(componentName, 0)
        logD(ACCESSIBILITY_TAG, "Admin receiver found: ${adminReceiver.packageName}")

        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            this.logE(ACCESSIBILITY_TAG, "Admin activation failed", e)
            ctx.showToast("Failed to open admin settings - check manifest")
        }
    }

    fun launchDragon(ctx: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            setPackage(ctx.packageName)
        }
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            this.logE(ACCESSIBILITY_TAG, "Launch failed", e)
            ctx.showToast("Failed to launch Dragon Launcher")
        }
    }
}
