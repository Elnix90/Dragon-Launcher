package org.elnix.dragonlauncher.ui.statusbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.elnix.dragonlauncher.common.serializables.StatusBarSerializable
import org.elnix.dragonlauncher.common.serializables.dummyAppModel
import org.elnix.dragonlauncher.common.utils.resolveShape
import org.elnix.dragonlauncher.services.DragonNotificationListenerService
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDrawerIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalIconShape

@Composable
fun StatusBarNotifications(
    element: StatusBarSerializable.Notifications
) {
    val ctx = LocalContext.current
    val icons = LocalDrawerIconsCache.current
    val appsViewModel = LocalAppsViewModel.current
    val packageNames by DragonNotificationListenerService.notifications.collectAsState()
    var hasPermission by remember { mutableStateOf(DragonNotificationListenerService.isPermissionGranted(ctx)) }

    val maxIcons = element.maxIcons

    LaunchedEffect(Unit) {
        while (isActive) {
            hasPermission = DragonNotificationListenerService.isPermissionGranted(ctx)
            delay(5_000L)
        }
    }

    if (!hasPermission) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = "Notifications",
            modifier = Modifier
                .size(18.dp)
                .clickable { DragonNotificationListenerService.openNotificationSettings(ctx) }
        )
        return
    }

    if (packageNames.isEmpty()) return

    val notificationsIcons = packageNames.take(maxIcons).map {
        val dummyAppModel = dummyAppModel(it)
        it to icons.getOrLazyCompute(dummyAppModel.iconCacheKey) {
            appsViewModel.reloadAppIcon(dummyAppModel)
        }
    }

    val showMoreNotificationsIcon = packageNames.size > maxIcons


    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        notificationsIcons.forEach { (pkg, bitmap) ->
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = pkg,
                    modifier = Modifier.size(18.dp).clip(LocalIconShape.current.resolveShape())
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = pkg,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(showMoreNotificationsIcon) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More notifications",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
