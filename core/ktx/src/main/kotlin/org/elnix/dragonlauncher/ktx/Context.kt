package org.elnix.dragonlauncher.ktx

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat

val Context.dp: Float
    get() = resources.displayMetrics.density


val Context.sp: Float
    get() = resources.displayMetrics.scaledDensity

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}


fun Context.tryStartActivity(intent: Intent, bundle: Bundle? = null): Boolean {
    return try {
        startActivity(intent, bundle)
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }
}


fun Context.getInstallSource(
    packageName: String
): InstallSourceInfoCompat {
    val pm = this.packageManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val installSourceInfo = pm.getInstallSourceInfo(packageName)
        return InstallSourceInfoCompat(
            originatingPackageName = installSourceInfo.originatingPackageName,
            initiatingPackageName = installSourceInfo.initiatingPackageName,
            installingPackageName = installSourceInfo.installingPackageName,
        )
    } else {
        val installerPackageName = pm.getInstallerPackageName(packageName)
        return InstallSourceInfoCompat(
            originatingPackageName = installerPackageName,
            initiatingPackageName = installerPackageName,
            installingPackageName = installerPackageName
        )
    }
}


data class InstallSourceInfoCompat(
    val originatingPackageName: String?,
    val initiatingPackageName: String?,
    val installingPackageName: String?
)