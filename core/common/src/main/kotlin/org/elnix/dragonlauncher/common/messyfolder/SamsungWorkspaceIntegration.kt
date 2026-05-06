package org.elnix.dragonlauncher.common.messyfolder

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SAMSUNG_INTEGRATION_TAG

object SamsungWorkspaceIntegration {

    private const val SECURE_FOLDER_PACKAGE = "com.samsung.knox.securefolder"
    private const val SECURE_FOLDER_ACTION = "com.samsung.knox.securefolder.LAUNCH_SECURE_FOLDER"

    fun isSamsungDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val isSamsung = manufacturer.equals("Samsung", ignoreCase = true) ||
            brand.equals("samsung", ignoreCase = true)

        logD(SAMSUNG_INTEGRATION_TAG) { "Device manufacturer: $manufacturer" }
        logD(SAMSUNG_INTEGRATION_TAG) { "Device brand: $brand" }
        logD(SAMSUNG_INTEGRATION_TAG) { "Is Samsung: $isSamsung" }

        return isSamsung
    }

    fun isSecureFolderAvailable(ctx: Context): Boolean {
        logD(SAMSUNG_INTEGRATION_TAG) { "Checking if Secure Folder is available..." }

        val hasSecureFolder = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ctx.packageManager.getPackageInfo(
                    SECURE_FOLDER_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                ctx.packageManager.getPackageInfo(SECURE_FOLDER_PACKAGE, 0)
            }
            true
        } catch (_: Exception) {
            false
        }

        logW(SAMSUNG_INTEGRATION_TAG) { "Secure Folder available: $hasSecureFolder" }
        return hasSecureFolder
    }

    fun resolveUseSecureFolder(
        ctx: Context,
        preferenceEnabled: Boolean
    ): Boolean {
        val isSamsung = isSamsungDevice()
        if (!isSamsung) return false

        val hasSecureFolder = isSecureFolderAvailable(ctx)
        if (preferenceEnabled && !hasSecureFolder) {
            logW(SAMSUNG_INTEGRATION_TAG) { "Secure Folder unavailable, falling back to Private Space" }
        }

        return preferenceEnabled && hasSecureFolder
    }

    fun openSecureFolder(
        ctx: Context,
        onFallback: () -> Unit
    ) {
        try {
            val intent = Intent(SECURE_FOLDER_ACTION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage(SECURE_FOLDER_PACKAGE)
            }
            ctx.startActivity(intent)
            logI(SAMSUNG_INTEGRATION_TAG) { "Opened Secure Folder" }
        } catch (e: Exception) {
            logE(SAMSUNG_INTEGRATION_TAG, e) { "Failed to launch Secure Folder" }
            onFallback()
        }
    }
}
