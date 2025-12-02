package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.drawerDataStore

object DrawerSettingsStore {

    data class DrawerSettingsBackup(
        val autoOpenSingleMatch: Boolean = true,
        val showAppIconsInDrawer: Boolean = true,
        val searchBarBottom: Boolean = true
    )


    private val AUTO_LAUNCH_SINGLE_MATCH = booleanPreferencesKey("auto_launch_single_match")
    fun getAutoLaunchSingleMatch(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { it[AUTO_LAUNCH_SINGLE_MATCH] ?: true }
    suspend fun setAutoLaunchSingleMatch(ctx: Context, enabled: Boolean) {
        ctx.drawerDataStore.edit { it[AUTO_LAUNCH_SINGLE_MATCH] = enabled }
    }

    private val SHOW_APP_ICONS_IN_DRAWER = booleanPreferencesKey("show_app_icons_in_drawer")
    fun getShowAppIconsInDrawer(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { it[SHOW_APP_ICONS_IN_DRAWER] ?: true }
    suspend fun setShowAppIconsInDrawer(ctx: Context, enabled: Boolean) {
        ctx.drawerDataStore.edit { it[SHOW_APP_ICONS_IN_DRAWER] = enabled }
    }

    private val SEARCH_BAR_BOTTOM = booleanPreferencesKey("search_bar_bottom")
    fun getSearchBarBottom(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { it[SEARCH_BAR_BOTTOM] ?: true }
    suspend fun setSearchBarBottom(ctx: Context, enabled: Boolean) {
        ctx.drawerDataStore.edit { it[SEARCH_BAR_BOTTOM] = enabled }
    }



    suspend fun resetAll(ctx: Context) {
        ctx.drawerDataStore.edit { prefs ->
            prefs.remove(AUTO_LAUNCH_SINGLE_MATCH)
            prefs.remove(SHOW_APP_ICONS_IN_DRAWER)
            prefs.remove(SEARCH_BAR_BOTTOM)
        }
    }

    suspend fun getAll(ctx: Context): Map<String, Boolean> {
        val prefs = ctx.drawerDataStore.data.first()
        val defaults = DrawerSettingsBackup()

        return buildMap {

            fun putIfNonDefault(key: String, value: Boolean?, default: Any?) {
                if (value != null && value != default) {
                    put(key, value)
                }
            }

            putIfNonDefault(AUTO_LAUNCH_SINGLE_MATCH.name, prefs[AUTO_LAUNCH_SINGLE_MATCH], defaults.autoOpenSingleMatch)
            putIfNonDefault(SHOW_APP_ICONS_IN_DRAWER.name, prefs[SHOW_APP_ICONS_IN_DRAWER], defaults.showAppIconsInDrawer)
            putIfNonDefault(SEARCH_BAR_BOTTOM.name, prefs[SEARCH_BAR_BOTTOM], defaults.searchBarBottom)
        }
    }


    suspend fun setAll(ctx: Context, backup: Map<String, Boolean>) {
        ctx.drawerDataStore.edit { prefs ->

            backup[AUTO_LAUNCH_SINGLE_MATCH.name]?.let {
                prefs[AUTO_LAUNCH_SINGLE_MATCH] = it
            }

            backup[SHOW_APP_ICONS_IN_DRAWER.name]?.let {
                prefs[SHOW_APP_ICONS_IN_DRAWER] = it
            }

            backup[SEARCH_BAR_BOTTOM.name]?.let {
                prefs[SEARCH_BAR_BOTTOM] = it
            }
        }
    }
}