package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BackupTypeException
import org.elnix.dragonlauncher.data.drawerDataStore

object DrawerSettingsStore {

    // -------------------------------------------------------------------------
    // Backup data class
    // -------------------------------------------------------------------------
    private data class DrawerSettingsBackup(
        val autoOpenSingleMatch: Boolean = true,
        val showAppIconsInDrawer: Boolean = true,
        val showAppLabelInDrawer: Boolean = true,
        val searchBarBottom: Boolean = false,
        val gridSize: Int = 6,
        val initialPAge: Int = 0
    )

    private val defaults = DrawerSettingsBackup()

    private object Keys {
        const val AUTO_OPEN_SINGLE_MATCH = "autoOpenSingleMatch"
        const val SHOW_APP_ICONS_IN_DRAWER = "showAppIconsInDrawer"
        const val SHOW_APP_LABEL_IN_DRAWER = "showAppLabelInDrawer"
        const val SEARCH_BAR_BOTTOM = "searchBarBottom"
        const val GRID_SIZE = "gridSize"

        const val INITIAL_PAGE = "initialPage"
    }


    // -------------------------------------------------------------------------
    // Keys
    // -------------------------------------------------------------------------
    private val AUTO_OPEN_SINGLE_MATCH =
        booleanPreferencesKey(Keys.AUTO_OPEN_SINGLE_MATCH)

    private val SHOW_APP_ICONS_IN_DRAWER =
        booleanPreferencesKey(Keys.SHOW_APP_ICONS_IN_DRAWER)

    private val SHOW_APP_LABEL_IN_DRAWER =
        booleanPreferencesKey(Keys.SHOW_APP_LABEL_IN_DRAWER)


    private val SEARCH_BAR_BOTTOM =
        booleanPreferencesKey(Keys.SEARCH_BAR_BOTTOM)

    private val GRID_SIZE =
        intPreferencesKey(Keys.GRID_SIZE)

    private val INITIAL_PAGE =
        intPreferencesKey(Keys.INITIAL_PAGE)


    // -------------------------------------------------------------------------
    // Accessors + Mutators
    // -------------------------------------------------------------------------
    fun getAutoLaunchSingleMatch(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { prefs ->
            prefs[AUTO_OPEN_SINGLE_MATCH] ?: defaults.autoOpenSingleMatch
        }

    suspend fun setAutoLaunchSingleMatch(ctx: Context, enabled: Boolean) {
        ctx.drawerDataStore.edit { it[AUTO_OPEN_SINGLE_MATCH] = enabled }
    }

    fun getShowAppIconsInDrawer(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { prefs ->
            prefs[SHOW_APP_ICONS_IN_DRAWER] ?: defaults.showAppIconsInDrawer
        }

    suspend fun setShowAppIconsInDrawer(ctx: Context, enabled: Boolean) {
        ctx.drawerDataStore.edit { it[SHOW_APP_ICONS_IN_DRAWER] = enabled }
    }

    fun getShowAppLabelsInDrawer(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { prefs ->
            prefs[SHOW_APP_LABEL_IN_DRAWER] ?: defaults.showAppLabelInDrawer
        }

    suspend fun setShowAppLabelsInDrawer(ctx: Context, enabled: Boolean) {
        ctx.drawerDataStore.edit { it[SHOW_APP_LABEL_IN_DRAWER] = enabled }
    }

    fun getSearchBarBottom(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { prefs ->
            prefs[SEARCH_BAR_BOTTOM] ?: defaults.searchBarBottom
        }

    suspend fun setSearchBarBottom(ctx: Context, enabled: Boolean) {
        ctx.drawerDataStore.edit { it[SEARCH_BAR_BOTTOM] = enabled }
    }

    fun getGridSize(ctx: Context): Flow<Int> =
        ctx.drawerDataStore.data.map { prefs ->
            prefs[GRID_SIZE] ?: defaults.gridSize
        }

    suspend fun setGridSize(ctx: Context, size: Int) {
        ctx.drawerDataStore.edit { it[GRID_SIZE] = size }
    }

    fun getInitialPage(ctx: Context): Flow<Int> =
        ctx.drawerDataStore.data.map { prefs ->
            prefs[INITIAL_PAGE] ?: defaults.gridSize
        }

    suspend fun setInitialPage(ctx: Context, page: Int) {
        ctx.drawerDataStore.edit { it[INITIAL_PAGE] = page }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------
    suspend fun resetAll(ctx: Context) {
        ctx.drawerDataStore.edit { prefs ->
            prefs.remove(AUTO_OPEN_SINGLE_MATCH)
            prefs.remove(SHOW_APP_ICONS_IN_DRAWER)
            prefs.remove(SHOW_APP_LABEL_IN_DRAWER)
            prefs.remove(SEARCH_BAR_BOTTOM)
            prefs.remove(GRID_SIZE)
            prefs.remove(INITIAL_PAGE)
        }
    }

    // -------------------------------------------------------------------------
    // Backup export
    // -------------------------------------------------------------------------
    suspend fun getAll(ctx: Context): Map<String, String> {
        val prefs = ctx.drawerDataStore.data.first()

        return buildMap {

            fun putIfNonDefault(key: String, value: Any?, defaultVal: Any) {
                if (value != null && value != defaultVal) {
                    put(key, value.toString())
                }
            }

            putIfNonDefault(
                Keys.AUTO_OPEN_SINGLE_MATCH,
                prefs[AUTO_OPEN_SINGLE_MATCH],
                defaults.autoOpenSingleMatch
            )

            putIfNonDefault(
                Keys.SHOW_APP_ICONS_IN_DRAWER,
                prefs[SHOW_APP_ICONS_IN_DRAWER],
                defaults.showAppIconsInDrawer
            )

            putIfNonDefault(
                Keys.SHOW_APP_LABEL_IN_DRAWER,
                prefs[SHOW_APP_LABEL_IN_DRAWER],
                defaults.showAppLabelInDrawer
            )

            putIfNonDefault(
                Keys.SEARCH_BAR_BOTTOM,
                prefs[SEARCH_BAR_BOTTOM],
                defaults.searchBarBottom
            )

            putIfNonDefault(
                Keys.GRID_SIZE,
                prefs[GRID_SIZE],
                defaults.gridSize
            )

            putIfNonDefault(
                Keys.INITIAL_PAGE,
                prefs[INITIAL_PAGE],
                defaults.initialPAge
            )
        }
    }

    // -------------------------------------------------------------------------
    // Backup import
    // -------------------------------------------------------------------------
    suspend fun setAll(ctx: Context, raw: Map<String, Any?>) {

        fun getBooleanStrict(key: String): Boolean {
            val v = raw[key] ?: return defaults.run {
                when (key) {
                    Keys.AUTO_OPEN_SINGLE_MATCH -> autoOpenSingleMatch
                    Keys.SHOW_APP_ICONS_IN_DRAWER -> showAppIconsInDrawer
                    Keys.SHOW_APP_LABEL_IN_DRAWER -> showAppLabelInDrawer
                    Keys.SEARCH_BAR_BOTTOM -> searchBarBottom
                    else -> throw BackupTypeException(
                        key, "Boolean", null, null
                    )
                }
            }

            return v as? Boolean ?: throw BackupTypeException(
                key = key,
                expected = "Boolean",
                actual = v::class.simpleName,
                value = v
            )
        }

        fun getIntStrict(key: String): Int {
            val v = raw[key] ?: return defaults.run {
                when (key) {
                    Keys.GRID_SIZE -> gridSize
                    Keys.INITIAL_PAGE -> initialPAge
                    else -> throw BackupTypeException(
                        key, "Int", null, null
                    )
                }
            }

            return when (v) {
                is Int -> v
                is Number -> v.toInt()
                is String -> v.toIntOrNull()
                    ?: throw BackupTypeException(
                        key = key,
                        expected = "Int",
                        actual = "String",
                        value = v
                    )
                else -> throw BackupTypeException(
                    key = key,
                    expected = "Int",
                    actual = v::class.simpleName,
                    value = v
                )
            }
        }

        val backup = DrawerSettingsBackup(
            autoOpenSingleMatch = getBooleanStrict(Keys.AUTO_OPEN_SINGLE_MATCH),
            showAppIconsInDrawer = getBooleanStrict(Keys.SHOW_APP_ICONS_IN_DRAWER),
            showAppLabelInDrawer = getBooleanStrict(Keys.SHOW_APP_LABEL_IN_DRAWER),
            searchBarBottom = getBooleanStrict(Keys.SEARCH_BAR_BOTTOM),
            gridSize = getIntStrict(Keys.GRID_SIZE),
            initialPAge = getIntStrict(Keys.INITIAL_PAGE)
        )

        ctx.drawerDataStore.edit { prefs ->
            prefs[AUTO_OPEN_SINGLE_MATCH] = backup.autoOpenSingleMatch
            prefs[SHOW_APP_ICONS_IN_DRAWER] = backup.showAppIconsInDrawer
            prefs[SHOW_APP_LABEL_IN_DRAWER] = backup.showAppLabelInDrawer
            prefs[SEARCH_BAR_BOTTOM] = backup.searchBarBottom
            prefs[GRID_SIZE] = backup.gridSize
            prefs[INITIAL_PAGE] = backup.initialPAge
        }
    }
}
