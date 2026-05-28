@file:Suppress("DEPRECATION")

package org.elnix.dragonlauncher.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.net.toUri
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.common.serializables.ExtensionModel
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.EXTENSION_MANAGER_TAG
import org.elnix.dragonlauncher.common.messyfolder.PackageManagerCompat
import org.elnix.dragonlauncher.common.messyfolder.openUrl
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore

object ExtensionManager {

    fun installExtension(ctx: Context, extension: ExtensionModel) {
        ctx.openUrl(extension.downloadUrl)
    }


    fun installApk(ctx: Context, uri: Uri) {
        try {
            if (!ctx.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = "package:${ctx.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(intent)
                ctx.showToast("Please allow unknown app installs first")
                return
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            logE(EXTENSION_MANAGER_TAG, e) { "Failed to install APK" }
            ctx.showToast("Failed to install APK")
        }
    }

    fun isExtensionInstalled(ctx: Context, packageNameOrId: String): Boolean {
        val pmCompat = PackageManagerCompat(ctx.packageManager, ctx)
        logD(EXTENSION_MANAGER_TAG) { "Checking extension installed for: $packageNameOrId" }

        val disableSigCheck = kotlinx.coroutines.runBlocking {
            DebugSettingsStore.disableExtensionSignatureCheck.get(ctx)
        }

        try {

            // Signature check
            if (!disableSigCheck) {
                @Suppress("DEPRECATION")
                val myPkgInfo = ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNATURES)

                @Suppress("DEPRECATION")
                val targetPkgInfo = ctx.packageManager.getPackageInfo(packageNameOrId, PackageManager.GET_SIGNATURES)

                val mySignatures = myPkgInfo.signatures
                val targetSignatures = targetPkgInfo.signatures

                val mySig = mySignatures?.firstOrNull()?.toCharsString()
                val targetSig = targetSignatures?.firstOrNull()?.toCharsString()

                if (mySig == null || mySig != targetSig) {
                    logW(EXTENSION_MANAGER_TAG) { "Signature mismatch for $packageNameOrId! Blocking detection. Enable 'Disable extension signature check' in debug to bypass." }
                    return false
                }
                return true
            } else {
                val isInstalled = pmCompat.isPackageInstalled(packageNameOrId)
                if (isInstalled) {
                    logD(EXTENSION_MANAGER_TAG) { "App installed: $packageNameOrId" }
                } else {
                    logD(EXTENSION_MANAGER_TAG) { "App not installed: $packageNameOrId" }
                }

                return isInstalled
            }

        } catch (_: Exception) {
            return false
        }
    }
}
