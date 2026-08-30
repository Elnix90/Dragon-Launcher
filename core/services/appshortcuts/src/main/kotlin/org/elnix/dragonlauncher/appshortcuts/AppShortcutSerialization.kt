package org.elnix.dragonlauncher.appshortcuts

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import io.github.elnix90.logging.logE
import org.elnix.dragonlauncher.JSON_TAG
import org.elnix.dragonlauncher.base.model.serializables.Action

@SuppressLint("InlinedApi")
public fun deserialize(serialized: Action.LaunchShortcut, ctx: Context): ShortcutInfo? {
    try {
        val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        val packageName = serialized.packageName
        val id = serialized.shortcutId
        val user = serialized.user

        if (!launcherApps.hasShortcutHostPermission()) {
            return null
        } else {
            val query = LauncherApps.ShortcutQuery()
            query.setPackage(packageName)
            query.setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_CACHED or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED_BY_ANY_LAUNCHER
            )
            query.setShortcutIds(mutableListOf(id))
            val shortcuts =
                try {
                    launcherApps.getShortcuts(query, user)
                } catch (_: IllegalStateException) {
                    return null
                }
            return if (shortcuts.isNullOrEmpty()) {
                null
            } else {
                shortcuts[0]
            }
        }
    } catch (e: SecurityException) {
        logE(JSON_TAG, e) { "Failed to deserialize shortcut: $serialized" }
        return null
    }
}
