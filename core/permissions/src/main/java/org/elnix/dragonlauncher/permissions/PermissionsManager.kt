package org.elnix.dragonlauncher.permissions

import android.Manifest
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import io.github.elnix90.logging.logE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.elnix.dragonlauncher.PERMISSIONS_TAG
import org.elnix.dragonlauncher.ktx.checkPermission
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.ktx.tryStartActivity

public interface PermissionsManager {
    public fun requestPermission(ctx: AppCompatActivity, permissionGroup: PermissionGroup)

    /**
     * Check if this permission is granted right now without receiving further updates
     * about the granted state.
     * @return true if the given permission group is fully granted
     */
    public fun checkPermissionOnce(permissionGroup: PermissionGroup): Boolean

    public fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )

    public fun onResume() {
    }

    public fun hasPermission(permissionGroup: PermissionGroup): Flow<Boolean>

    public suspend fun hasPermissionBlocking(permissionGroup: PermissionGroup): Boolean

    /**
     * Special function for the Notification listener to report its status.
     * May not be called by anything else.
     */
    public fun reportNotificationListenerState(running: Boolean)

    /**
     * Special function for the accessibility service to report its status.
     * May not be called by anything else.
     */
    public fun reportAccessibilityServiceState(running: Boolean)
}

@Suppress("KotlinConstantConditions")
internal class PermissionsManagerImpl(
    private val ctx: Context
) : PermissionsManager {
    private val pendingPermissionRequests = mutableSetOf<PermissionGroup>()

    private val tasksPermissionState =
        MutableStateFlow(
            checkPermissionOnce(PermissionGroup.Tasks)
        )

    private val externalStoragePermissionState =
        MutableStateFlow(
            checkPermissionOnce(PermissionGroup.ExternalStorage)
        )

    private val usageStatPermissionState =
        MutableStateFlow(
            checkPermissionOnce(PermissionGroup.UsageStat)
        )

    private val notificationsPermissionState = MutableStateFlow(false)

    private val accessibilityPermissionState = MutableStateFlow(false)

    private val appShortcutsPermissionState =
        MutableStateFlow(
            checkPermissionOnce(PermissionGroup.AppShortcuts)
        )
    private val manageProfilesPermissionState =
        MutableStateFlow(
            checkPermissionOnce(PermissionGroup.ManageProfiles)
        )

    override fun requestPermission(ctx: AppCompatActivity, permissionGroup: PermissionGroup) {
        when (permissionGroup) {
            PermissionGroup.Tasks -> {
                ActivityCompat.requestPermissions(
                    ctx,
                    taskPermissions,
                    permissionGroup.ordinal
                )
            }

            PermissionGroup.ExternalStorage -> {
                if (isAtLeastApiLevel(Build.VERSION_CODES.R)) {
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).also {
                            it.data = "package:${ctx.packageName}".toUri()
                        }
                    ctx.tryStartActivity(intent)
                    pendingPermissionRequests.add(PermissionGroup.ExternalStorage)
                } else {
                    ActivityCompat.requestPermissions(
                        ctx,
                        externalStoragePermissions,
                        permissionGroup.ordinal
                    )
                }
            }

            PermissionGroup.Notifications -> {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } catch (e: ActivityNotFoundException) {
                    logE(PERMISSIONS_TAG, e) { "Failed to start notifications settings" }
                }
            }

            PermissionGroup.ManageProfiles,
            PermissionGroup.AppShortcuts
            -> {
                // TODO open default launcher settings
                if (isAtLeastApiLevel(29)) {
                    val roleManager = ctx.getSystemService<RoleManager>()
                    ctx.startActivityForResult(
                        roleManager!!.createRequestRoleIntent(RoleManager.ROLE_HOME),
                        permissionGroup.ordinal
                    )
                } else {
                    ctx.tryStartActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                }
                pendingPermissionRequests.add(PermissionGroup.AppShortcuts)
            }

            PermissionGroup.Accessibility -> {
                try {
                    ctx.tryStartActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    pendingPermissionRequests.add(PermissionGroup.Accessibility)
                } catch (e: ActivityNotFoundException) {
                    logE(PERMISSIONS_TAG, e) { "Failed to start accessibility settings" }
                }
            }

            PermissionGroup.UsageStat -> {
                ctx.tryStartActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                pendingPermissionRequests.add(PermissionGroup.UsageStat)
            }
        }
    }

    override fun checkPermissionOnce(permissionGroup: PermissionGroup): Boolean =
        when (permissionGroup) {
            PermissionGroup.Tasks -> {
                taskPermissions.all { ctx.checkPermission(it) }
            }

            PermissionGroup.ExternalStorage -> {
                if (isAtLeastApiLevel(Build.VERSION_CODES.R)) {
                    Environment.isExternalStorageManager()
                } else {
                    externalStoragePermissions.all { ctx.checkPermission(it) }
                }
            }

            PermissionGroup.Notifications -> {
                notificationsPermissionState.value
            }

            PermissionGroup.AppShortcuts -> {
                ctx.getSystemService<LauncherApps>()?.hasShortcutHostPermission() == true
            }

            PermissionGroup.ManageProfiles -> {
                if (isAtLeastApiLevel(29)) {
                    ctx.getSystemService<RoleManager>()?.isRoleHeld(RoleManager.ROLE_HOME) == true
                } else {
                    false
                }
            }

            PermissionGroup.Accessibility -> {
                accessibilityPermissionState.value
            }

            PermissionGroup.UsageStat -> {
                hasUsageStatsPermission(ctx)
            }
        }

    override fun hasPermission(permissionGroup: PermissionGroup): Flow<Boolean> =
        when (permissionGroup) {
            PermissionGroup.Tasks -> tasksPermissionState
            PermissionGroup.ExternalStorage -> externalStoragePermissionState
            PermissionGroup.Notifications -> notificationsPermissionState
            PermissionGroup.AppShortcuts -> appShortcutsPermissionState
            PermissionGroup.Accessibility -> accessibilityPermissionState
            PermissionGroup.ManageProfiles -> manageProfilesPermissionState
            PermissionGroup.UsageStat -> usageStatPermissionState
        }

    override suspend fun hasPermissionBlocking(permissionGroup: PermissionGroup): Boolean =
        hasPermission(
            permissionGroup
        ).first()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionGroup = PermissionGroup.entries.getOrNull(requestCode) ?: return
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (permissionGroup) {
            PermissionGroup.Tasks -> tasksPermissionState.value = granted
            PermissionGroup.ExternalStorage -> externalStoragePermissionState.value = granted
            PermissionGroup.Notifications -> notificationsPermissionState.value = granted
            PermissionGroup.AppShortcuts -> appShortcutsPermissionState.value = granted
            PermissionGroup.Accessibility -> accessibilityPermissionState.value = granted
            PermissionGroup.ManageProfiles -> manageProfilesPermissionState.value = granted
            PermissionGroup.UsageStat -> usageStatPermissionState.value = granted
        }
    }

    override fun onResume() {
        externalStoragePermissionState.value = checkPermissionOnce(PermissionGroup.ExternalStorage)
        appShortcutsPermissionState.value = checkPermissionOnce(PermissionGroup.AppShortcuts)
        manageProfilesPermissionState.value = checkPermissionOnce(PermissionGroup.ManageProfiles)
        usageStatPermissionState.value = checkPermissionOnce(PermissionGroup.UsageStat)
    }

    override fun reportNotificationListenerState(running: Boolean) {
        notificationsPermissionState.value = running
    }

    override fun reportAccessibilityServiceState(running: Boolean) {
        accessibilityPermissionState.value = running
    }

    companion object {
        private val taskPermissions = arrayOf("org.tasks.permission.READ_TASKS")
        private val externalStoragePermissions =
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

        private fun hasUsageStatsPermission(ctx: Context): Boolean {
            val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val uid = android.os.Process.myUid()
            val pkg = ctx.packageName

            val mode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        uid,
                        pkg
                    )
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        uid,
                        pkg
                    )
                }

            return mode == android.app.AppOpsManager.MODE_ALLOWED
        }
    }
}
