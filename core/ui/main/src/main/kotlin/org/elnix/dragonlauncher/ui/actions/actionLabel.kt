package org.elnix.dragonlauncher.ui.actions

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute.Settings.routeResId
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getFilePathFromUri
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel

@Composable
fun actionLabel(
    action: Action,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel()
): String {
    val ctx = LocalContext.current
    val pointsService = pointsViewModel.pointsService

    return when (action) {

        is Action.LaunchApp -> {
            val app by drawerViewModel.findOne(action.packageName, action.profile.userHandle).collectAsState(null)
            app?.label ?: action.packageName
        }

        is Action.LaunchShortcut -> {
            // Empty package = sentinel for "Pinned Shortcuts" chooser entry
            if (action.packageName.isEmpty()) {
                return stringResource(R.string.pinned_shortcuts)
            }

            val appLabel = actionLabel(Action.LaunchApp(action.packageName, Profile.dummy()))

            val shortcutLabel = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    drawerViewModel.queryAppShortcuts(action.packageName)
                        .firstOrNull { it.id == action.shortcutId }
                        ?.shortLabel
                        ?.toString()
                } else null
            } catch (_: Exception) {
                null
            }

            when {
                !shortcutLabel.isNullOrBlank() -> "$appLabel: $shortcutLabel"
                else -> appLabel
            }
        }


        is Action.OpenUrl -> action.url


        is Action.OpenDragonLauncherSettings -> "${stringResource(R.string.dragon_launcher_settings)} (${stringResource(routeResId(action.route))})"


        is Action.OpenFile ->
            ctx.getFilePathFromUri(action.uri.toUri())


        is Action.OpenCircleNest -> {
            pointsService.nests.value
                .find { it.id == action.nestId }
                ?.name
                ?.takeIf { it.trim().isNotEmpty() }
                ?: stringResource(R.string.open_nest)
        }

        is Action.RunAdbCommand -> action.command.trim().takeIf { it.isNotEmpty() } ?: stringResource(R.string.run_adb_command)

        else -> stringResource(action.resId)
    }
}
