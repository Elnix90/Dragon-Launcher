package org.elnix.dragonlauncher.ui.settings.permissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.components.BetaVersionType
import org.elnix.dragonlauncher.ui.components.BetaVersionWarning
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun PermissionsTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val permissionStates = remember { mutableStateMapOf<String, Boolean>() }

    fun checkPermissions() {
        val perms = listOf(
            Manifest.permission.QUERY_ALL_PACKAGES,
            Manifest.permission.REQUEST_DELETE_PACKAGES,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.BIND_APPWIDGET,
        ).let { list ->
            var updatedList = list
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                updatedList = updatedList + Manifest.permission.POST_NOTIFICATIONS
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                updatedList = updatedList + Manifest.permission.SCHEDULE_EXACT_ALARM
            }
            updatedList
        }

        perms.forEach { permission ->
            permissionStates[permission] = ContextCompat.checkSelfPermission(
                ctx,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Recheck permissions on user return
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // The activity resumes when the user returns from the Home settings screen
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the noAnimComposable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column {
        SettingsScaffold(
            title = stringResource(R.string.permissions),
            onBack = onBack,
            helpText = stringResource(R.string.permission_tab_help),
            onReset = null
        ) {
            item {
                BetaVersionWarning(BetaVersionType.Feature)
            }
            
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.special_system_access),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.reload),
                        modifier = Modifier
                            .clip(DragonShape)
                            .clickable { checkPermissions() }
                            .padding(5.dp)
                    )
                }
            }

            item {
                SwitchRow(
                    state = null,
                    onCheck = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        ctx.startActivity(intent)
                    },
                    title = stringResource(R.string.notification_access),
                    description = stringResource(R.string.notification_access_desc)
                )
            }

            item {
                SwitchRow(
                    state = ctx.packageManager.canRequestPackageInstalls(),
                    onCheck = {
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = "package:${ctx.packageName}".toUri()
                        }
                        ctx.startActivity(intent)
                    },
                    title = stringResource(R.string.install_from_unknown_source_permission),
                    description = stringResource(R.string.install_from_unknown_source_permission_desc)
                )
            }

            item {
                SwitchRow(
                    state = null,
                    onCheck = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        ctx.startActivity(intent)
                    },
                    title = stringResource(R.string.accessibility_service),
                    description = stringResource(R.string.accessibility_service_desc)

                )
            }

            item {
                SwitchRow(
                    state = null,
                    enabled = false,
                    onCheck = {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        ctx.startActivity(intent)
                    },
                    title = stringResource(R.string.usage_access),
                    description = stringResource(R.string.not_implemented)
//                    subText = stringResource(R.string.usage_access_desc)
                )
            }

            item {
                Text(
                    text = stringResource(R.string.android_permissions),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val androidPermissions = listOf(
                Manifest.permission.QUERY_ALL_PACKAGES,
                Manifest.permission.BIND_APPWIDGET,
//            ).let { list ->
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    list + Manifest.permission.POST_NOTIFICATIONS
//                } else list
//            }

            androidPermissions.forEach { permission ->
                item {
                    val isGranted = permissionStates[permission] ?: false
                    SwitchRow(
                        state = isGranted,
                        onCheck = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", ctx.packageName, null)
                                }
                            ctx.startActivity(intent)
                        },
                        title = permission.substringAfterLast("."),
                        description = stringResource(R.string.manage_in_system_settings)
                    )
                }
            }
        }
    }
}
