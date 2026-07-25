package org.elnix.dragonlauncher.ui.statusbar

import android.os.Process
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.services.openNotificationSettings
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel

@Composable
fun StatusBarNotifications(
    element: StatusBar.Notifications,
    drawerViewModel: DrawerViewModel = activityViewModel(),
) {
    val ctx = LocalContext.current

    val notifications = drawerViewModel.notifications

    val hasNotificationPermission by drawerViewModel.hasPermission(PermissionGroup.Notifications).collectAsState(false)

    if (!hasNotificationPermission) {
        Icon(
            painter = painterResource(R.drawable.notification_important),
            contentDescription = "Notifications",
            modifier = Modifier
                .size(18.dp)
                .clickable { openNotificationSettings(ctx) }
        )
        return
    } else if (notifications.isNullOrEmpty()) return

    val maxIcons = element.maxIcons
    val showMoreNotificationsIcon = notifications.size > maxIcons

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        notifications.take(maxIcons).forEach { notification ->
            val pkg = notification?.packageName ?: "Unknown"
            val user = notification?.user ?: Process.myUserHandle()

            val app by drawerViewModel.findOne(pkg, user).collectAsState(null)


            app?.let {
                AppIcon(it, 10.dp)
            }
        }

        AnimatedVisibility(showMoreNotificationsIcon) {
            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "More notifications",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
