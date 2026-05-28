package org.elnix.dragonlauncher.common.search

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.UserHandle
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TintedIconLayer
import org.elnix.dragonlauncher.base.profiles.Profile
import org.elnix.dragonlauncher.common.serializables.CacheKey
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.logging.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.logging.logE
import java.io.File
import java.text.Collator

abstract class Application : Comparable<Application> {

    abstract val label: String
    abstract val labelOverride: String?
    abstract fun overrideLabel(label: String): Application

    abstract val category: AppCategory

    abstract val profile: Profile
    val userSerialNumber: Long
        get() = profile.serial
    val user: UserHandle
        get() = profile.userHandle

    abstract val isSystem: Boolean
    abstract val isLaunchable: Boolean
    abstract val componentName: ComponentName


    abstract val packageName: String

    abstract val versionName: String?

    val isPrivate: Boolean
        get() = profile.type == Profile.Type.Private

    val isWork: Boolean
        get() = profile.type == Profile.Type.Work

    /**
     * Cached result of the normalized label.
     * First string is the normalizer ID
     * Second string is the normalized label
     */
    abstract var cachedNormalizerResult: Pair<String, String>?


    // TODO
    val action = SwipeAction.LaunchApp(packageName, isPrivate, userSerialNumber.toInt())

    val key: CacheKey
        get() = CacheKey(componentName, user)

    abstract suspend fun loadIcon(themed: Boolean): LauncherIcon?

    fun launch(ctx: Context, options: Bundle?): Boolean {
        val launcherApps = ctx.getSystemService<LauncherApps>()!!
        if (isAtLeastApiLevel(31)) {
            options?.putInt("android.activity.splashScreenStyle", 1)
        }
        try {
            launcherApps.startMainActivity(
                componentName,
                user,
                null,
                options
            )
        } catch (e: SecurityException) {
            logE(APP_LAUNCH_TAG, e) { "Could not launch app" }
            return false
        } catch (e: ActivityNotFoundException) {
            logE(APP_LAUNCH_TAG, e) { "Could not launch app" }
            return false
        }
        return true
    }


    fun getPlaceholderIcon(ctx: Context): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = TintedIconLayer(
                icon = ContextCompat.getDrawable(ctx, R.drawable.android)!!,
                scale = 0.65f,
                color = 0xff3dda84.toInt(),
            ),
            backgroundLayer = ColorLayer(0xff3dda84.toInt())
        )
    }


    abstract fun getStoreDetails(ctx: Context): StoreLink?

    fun uninstall(ctx: Context) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = "package:${componentName.packageName}".toUri()
        ctx.startActivity(intent)
    }

    fun openAppDetails(ctx: Context) {
        val launcherApps = ctx.getSystemService<LauncherApps>()!!

        launcherApps.startAppDetailsActivity(
            componentName,
            user,
            null,
            null
        )
    }

    suspend fun shareApkFile(ctx: Context) {
        val launcherApps = ctx.getSystemService<LauncherApps>()!!
        val fileCopy = File(
            ctx.cacheDir,
            "${componentName.packageName}-${versionName}.apk"
        )
        withContext(Dispatchers.IO) {
            try {
                val info = launcherApps.getApplicationInfo(componentName.packageName, 0, user)
                val file = File(info.publicSourceDir)

                try {
                    file.copyTo(fileCopy, false)
                } catch (_: FileAlreadyExistsException) {
                    // Do nothing. If the file is already there we don't have to copy it again.
                }
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val uri = FileProvider.getUriForFile(
            ctx,
            ctx.applicationContext.packageName + ".fileprovider",
            fileCopy
        )
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "application/vnd.android.package-archive"
        withContext(Dispatchers.Main) {
            ctx.startActivity(Intent.createChooser(shareIntent, null))
        }
    }


    fun getActivityInfo(ctx: Context): ActivityInfo? {
        return try {
            ctx.packageManager.getActivityInfo(componentName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun compareTo(other: Application): Int {
        val label1 = labelOverride ?: label
        val label2 = other.labelOverride ?: other.label
        return Collator.getInstance().apply { strength = Collator.SECONDARY }
            .compare(label1, label2)
    }

    companion object {

        fun getStoreLinkForInstaller(
            installerPackage: String?,
            packageName: String?
        ): StoreLink? {
            if (packageName == null) return null
            return when (installerPackage) {
                "de.amazon.mShop.android", "com.amazon.venezia" -> {
                    StoreLink(
                        "Amazon App Shop",
                        "http://www.amazon.com/gp/mas/dl/android?p=${packageName}"
                    )
                }

                "com.android.vending" -> {
                    StoreLink(
                        "Google Play Store",
                        "https://play.google.com/store/apps/details?id=${packageName}"
                    )
                }

                "org.fdroid.fdroid", "com.aurora.adroid" -> {
                    StoreLink(
                        "F-Droid",
                        "https://f-droid.org/packages/${packageName}"
                    )
                }

                else -> null
            }
        }

        fun getPackageVersionName(ctx: Context, packageName: String): String? {
            return try {
                ctx.packageManager.getPackageInfo(packageName, 0).versionName
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }

//        fun isSuspended(ctx: Context, packageName: String): Boolean {
//            return try {
//                ctx.packageManager.getApplicationInfo(
//                    packageName,
//                    0
//                ).flags and ApplicationInfo.FLAG_SUSPENDED != 0
//            } catch (e: PackageManager.NameNotFoundException) {
//                false
//            }
//        }
    }
}

data class StoreLink(
    val label: String,
    val url: String
)
