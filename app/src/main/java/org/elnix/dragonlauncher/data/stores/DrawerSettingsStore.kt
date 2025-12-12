package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BackupTypeException
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.drawerDataStore
import org.elnix.dragonlauncher.data.helpers.DrawerActions

object DrawerSettingsStore : BaseSettingsStore() {
    override val name: String = "Drawer"

    // -------------------------------------------------------------------------
    // Backup data class
    // -------------------------------------------------------------------------
    private data class DrawerSettingsBackup(
        val autoOpenSingleMatch: Boolean = true,
        val showAppIconsInDrawer: Boolean = true,
        val showAppLabelInDrawer: Boolean = true,
        val searchBarBottom: Boolean = true,
        val autoShowKeyboardOnDrawer: Boolean = true,
        val clickEmptySpaceToRaiseKeyboard: Boolean = false,
        val gridSize: Int = 6,
        val initialPage: Int = 0,
        val leftDrawerAction: DrawerActions = DrawerActions.TOGGLE_KB,
        val rightDrawerAction: DrawerActions = DrawerActions.CLOSE,
        val leftDrawerWidth: Int = 50,
        val rightDrawerWidth: Int = 50
    )

    private val defaults = DrawerSettingsBackup()

    // -------------------------------------------------------------------------
    // Keys
    // -------------------------------------------------------------------------
    private object Keys {
        const val AUTO_OPEN_SINGLE_MATCH = "autoOpenSingleMatch"
        const val SHOW_APP_ICONS_IN_DRAWER = "showAppIconsInDrawer"
        const val SHOW_APP_LABEL_IN_DRAWER = "showAppLabelInDrawer"
        const val SEARCH_BAR_BOTTOM = "searchBarBottom"
        const val AUTO_SHOW_KEYBOARD_ON_DRAWER = "autoShowKeyboardOnDrawer"
        const val CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD = "clickEmptySpaceToRaiseKeyboard"
        const val GRID_SIZE = "gridSize"
        const val INITIAL_PAGE = "initialPage"
        const val LEFT_DRAWER_ACTION = "leftDrawerAction"
        const val RIGHT_DRAWER_ACTION = "rightDrawerAction"
        const val LEFT_DRAWER_WIDTH = "leftDrawerWidth"
        const val RIGHT_DRAWER_WIDTH = "rightDrawerWidth"
    }

    private val AUTO_OPEN_SINGLE_MATCH = booleanPreferencesKey(Keys.AUTO_OPEN_SINGLE_MATCH)
    private val SHOW_APP_ICONS_IN_DRAWER = booleanPreferencesKey(Keys.SHOW_APP_ICONS_IN_DRAWER)
    private val SHOW_APP_LABEL_IN_DRAWER = booleanPreferencesKey(Keys.SHOW_APP_LABEL_IN_DRAWER)
    private val SEARCH_BAR_BOTTOM = booleanPreferencesKey(Keys.SEARCH_BAR_BOTTOM)
    private val AUTO_SHOW_KEYBOARD_ON_DRAWER = booleanPreferencesKey(Keys.AUTO_SHOW_KEYBOARD_ON_DRAWER)
    private val CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD = booleanPreferencesKey(Keys.CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD)
    private val GRID_SIZE = intPreferencesKey(Keys.GRID_SIZE)
    private val INITIAL_PAGE = intPreferencesKey(Keys.INITIAL_PAGE)
    private val LEFT_DRAWER_ACTION = stringPreferencesKey(Keys.LEFT_DRAWER_ACTION)
    private val RIGHT_DRAWER_ACTION = stringPreferencesKey(Keys.RIGHT_DRAWER_ACTION)
    private val LEFT_DRAWER_WIDTH = intPreferencesKey(Keys.LEFT_DRAWER_WIDTH)
    private val RIGHT_DRAWER_WIDTH = intPreferencesKey(Keys.RIGHT_DRAWER_WIDTH)

    // -------------------------------------------------------------------------
    // Accessors + Mutators
    // -------------------------------------------------------------------------
    fun getAutoLaunchSingleMatch(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { it[AUTO_OPEN_SINGLE_MATCH] ?: defaults.autoOpenSingleMatch }

    suspend fun setAutoLaunchSingleMatch(ctx: Context, v: Boolean) {
        ctx.drawerDataStore.edit { it[AUTO_OPEN_SINGLE_MATCH] = v }
    }

    fun getShowAppIconsInDrawer(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { it[SHOW_APP_ICONS_IN_DRAWER] ?: defaults.showAppIconsInDrawer }

    suspend fun setShowAppIconsInDrawer(ctx: Context, v: Boolean) {
        ctx.drawerDataStore.edit { it[SHOW_APP_ICONS_IN_DRAWER] = v }
    }

    fun getShowAppLabelsInDrawer(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { it[SHOW_APP_LABEL_IN_DRAWER] ?: defaults.showAppLabelInDrawer }

    suspend fun setShowAppLabelsInDrawer(ctx: Context, v: Boolean) {
        ctx.drawerDataStore.edit { it[SHOW_APP_LABEL_IN_DRAWER] = v }
    }

    fun getSearchBarBottom(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map { it[SEARCH_BAR_BOTTOM] ?: defaults.searchBarBottom }

    suspend fun setSearchBarBottom(ctx: Context, v: Boolean) {
        ctx.drawerDataStore.edit { it[SEARCH_BAR_BOTTOM] = v }
    }

    fun getAutoShowKeyboardOnDrawer(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map {
            it[AUTO_SHOW_KEYBOARD_ON_DRAWER] ?: defaults.autoShowKeyboardOnDrawer
        }

    suspend fun setAutoShowKeyboardOnDrawer(ctx: Context, v: Boolean) {
        ctx.drawerDataStore.edit { it[AUTO_SHOW_KEYBOARD_ON_DRAWER] = v }
    }

    fun getClickEmptySpaceToRaiseKeyboard(ctx: Context): Flow<Boolean> =
        ctx.drawerDataStore.data.map {
            it[CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD] ?: defaults.clickEmptySpaceToRaiseKeyboard
        }

    suspend fun setClickEmptySpaceToRaiseKeyboard(ctx: Context, v: Boolean) {
        ctx.drawerDataStore.edit { it[CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD] = v }
    }

    fun getGridSize(ctx: Context): Flow<Int> =
        ctx.drawerDataStore.data.map { it[GRID_SIZE] ?: defaults.gridSize }

    suspend fun setGridSize(ctx: Context, size: Int) {
        ctx.drawerDataStore.edit { it[GRID_SIZE] = size }
    }

    fun getInitialPage(ctx: Context): Flow<Int> =
        ctx.drawerDataStore.data.map { it[INITIAL_PAGE] ?: defaults.initialPage }

    suspend fun setInitialPage(ctx: Context, page: Int) {
        ctx.drawerDataStore.edit { it[INITIAL_PAGE] = page }
    }

    fun getLeftDrawerAction(ctx: Context): Flow<DrawerActions> =
        ctx.drawerDataStore.data.map { DrawerActions.valueOf(it[LEFT_DRAWER_ACTION] ?: defaults.leftDrawerAction.name) }

    suspend fun setLeftDrawerAction(ctx: Context, action: DrawerActions) {
        ctx.drawerDataStore.edit { it[LEFT_DRAWER_ACTION] = action.name }
    }

    fun getRightDrawerAction(ctx: Context): Flow<DrawerActions> =
        ctx.drawerDataStore.data.map { DrawerActions.valueOf(it[RIGHT_DRAWER_ACTION] ?: defaults.rightDrawerAction.name) }

    suspend fun setRightDrawerAction(ctx: Context, action: DrawerActions) {
        ctx.drawerDataStore.edit { it[RIGHT_DRAWER_ACTION] = action.name }
    }


    fun getLeftDrawerWidth(ctx: Context): Flow<Dp> =
        ctx.drawerDataStore.data.map { it[LEFT_DRAWER_WIDTH]?.dp ?: defaults.leftDrawerWidth.dp }

    suspend fun setLeftDrawerWidth(ctx: Context, width: Int) {
        ctx.drawerDataStore.edit { it[LEFT_DRAWER_WIDTH] = width }
    }

    fun getRightDrawerWidth(ctx: Context): Flow<Dp> =
        ctx.drawerDataStore.data.map { it[RIGHT_DRAWER_WIDTH]?.dp ?: defaults.rightDrawerWidth.dp }
?
    suspend fun setRightDrawerWidth(ctx: Context, width: Int) {
        ctx.drawerDataStore.edit { it[RIGHT_DRAWER_WIDTH] = width }
    }


    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------
    override suspend fun resetAll(ctx: Context) {
        ctx.drawerDataStore.edit { prefs ->
            prefs.remove(AUTO_OPEN_SINGLE_MATCH)
            prefs.remove(SHOW_APP_ICONS_IN_DRAWER)
            prefs.remove(SHOW_APP_LABEL_IN_DRAWER)
            prefs.remove(SEARCH_BAR_BOTTOM)
            prefs.remove(AUTO_SHOW_KEYBOARD_ON_DRAWER)
            prefs.remove(CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD)
            prefs.remove(GRID_SIZE)
            prefs.remove(INITIAL_PAGE)
            prefs.remove(LEFT_DRAWER_ACTION)
            prefs.remove(RIGHT_DRAWER_ACTION)
            prefs.remove(LEFT_DRAWER_WIDTH)
            prefs.remove(RIGHT_DRAWER_WIDTH)
        }
    }

    // -------------------------------------------------------------------------
    // Backup export
    // -------------------------------------------------------------------------
    suspend fun getAll(ctx: Context): Map<String, String> {
        val prefs = ctx.drawerDataStore.data.first()

        return buildMap {

            fun putIfNonDefault(key: String, value: Any?, def: Any) {
                if (value != null && value != def) {
                    put(key, value.toString())
                }
            }

            putIfNonDefault(Keys.AUTO_OPEN_SINGLE_MATCH, prefs[AUTO_OPEN_SINGLE_MATCH], defaults.autoOpenSingleMatch)
            putIfNonDefault(Keys.SHOW_APP_ICONS_IN_DRAWER, prefs[SHOW_APP_ICONS_IN_DRAWER], defaults.showAppIconsInDrawer)
            putIfNonDefault(Keys.SHOW_APP_LABEL_IN_DRAWER, prefs[SHOW_APP_LABEL_IN_DRAWER], defaults.showAppLabelInDrawer)
            putIfNonDefault(Keys.SEARCH_BAR_BOTTOM, prefs[SEARCH_BAR_BOTTOM], defaults.searchBarBottom)
            putIfNonDefault(Keys.AUTO_SHOW_KEYBOARD_ON_DRAWER, prefs[AUTO_SHOW_KEYBOARD_ON_DRAWER], defaults.autoShowKeyboardOnDrawer)
            putIfNonDefault(Keys.CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD, prefs[CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD], defaults.clickEmptySpaceToRaiseKeyboard)
            putIfNonDefault(Keys.GRID_SIZE, prefs[GRID_SIZE], defaults.gridSize)
            putIfNonDefault(Keys.INITIAL_PAGE, prefs[INITIAL_PAGE], defaults.initialPage)
            putIfNonDefault(Keys.LEFT_DRAWER_ACTION, prefs[LEFT_DRAWER_ACTION],  defaults.leftDrawerAction)
            putIfNonDefault(Keys.LEFT_DRAWER_WIDTH, prefs[RIGHT_DRAWER_WIDTH],  defaults.leftDrawerWidth)
            putIfNonDefault(Keys.RIGHT_DRAWER_WIDTH, prefs[RIGHT_DRAWER_WIDTH],  defaults.leftDrawerWidth)
        }
    }

    // -------------------------------------------------------------------------
    // Backup import
    // -------------------------------------------------------------------------
    suspend fun setAll(ctx: Context, raw: Map<String, Any?>) {

        fun getBooleanStrict(key: String): Boolean {
            val v = raw[key] ?: return when (key) {
                Keys.AUTO_OPEN_SINGLE_MATCH -> defaults.autoOpenSingleMatch
                Keys.SHOW_APP_ICONS_IN_DRAWER -> defaults.showAppIconsInDrawer
                Keys.SHOW_APP_LABEL_IN_DRAWER -> defaults.showAppLabelInDrawer
                Keys.SEARCH_BAR_BOTTOM -> defaults.searchBarBottom
                Keys.AUTO_SHOW_KEYBOARD_ON_DRAWER -> defaults.autoShowKeyboardOnDrawer
                Keys.CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD -> defaults.clickEmptySpaceToRaiseKeyboard
                else -> throw BackupTypeException(key, "Boolean", null, null)
            }

            return when (v) {
                is Boolean -> v
                is Number -> v.toInt() != 0
                is String -> when (v.trim().lowercase()) {
                    "true", "1", "yes", "y", "on" -> true
                    "false", "0", "no", "n", "off" -> false
                    else -> throw BackupTypeException(key, "Boolean", "String", v)
                }
                else -> throw BackupTypeException(key, "Boolean", v::class.simpleName, v)
            }
        }

        fun getIntStrict(key: String): Int {
            val v = raw[key] ?: return when (key) {
                Keys.GRID_SIZE -> defaults.gridSize
                Keys.INITIAL_PAGE -> defaults.initialPage
                Keys.LEFT_DRAWER_WIDTH -> defaults.leftDrawerWidth
                Keys.RIGHT_DRAWER_WIDTH -> defaults.rightDrawerWidth
                else -> throw BackupTypeException(key, "Int", null, null)
            }

            return when (v) {
                is Int -> v
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: throw BackupTypeException(key, "Int", "String", v)
                else -> throw BackupTypeException(key, "Int", v::class.simpleName, v)
            }
        }


        fun getDrawerActionStrict(key: String): DrawerActions {
            val v = raw[key] ?: return when (key) {
                Keys.LEFT_DRAWER_ACTION -> defaults.leftDrawerAction
                Keys.RIGHT_DRAWER_ACTION -> defaults.rightDrawerAction
                else -> throw BackupTypeException(key, "DrawerAction", null, null)
            }

            return when (v) {
                is String -> DrawerActions.valueOf(v)
                else -> throw BackupTypeException(key, "DrawerAction", v::class.simpleName, v)
            }
        }

        val backup = DrawerSettingsBackup(
            autoOpenSingleMatch = getBooleanStrict(Keys.AUTO_OPEN_SINGLE_MATCH),
            showAppIconsInDrawer = getBooleanStrict(Keys.SHOW_APP_ICONS_IN_DRAWER),
            showAppLabelInDrawer = getBooleanStrict(Keys.SHOW_APP_LABEL_IN_DRAWER),
            searchBarBottom = getBooleanStrict(Keys.SEARCH_BAR_BOTTOM),
            autoShowKeyboardOnDrawer = getBooleanStrict(Keys.AUTO_SHOW_KEYBOARD_ON_DRAWER),
            clickEmptySpaceToRaiseKeyboard = getBooleanStrict(Keys.CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD),
            gridSize = getIntStrict(Keys.GRID_SIZE),
            initialPage = getIntStrict(Keys.INITIAL_PAGE),
            leftDrawerAction = getDrawerActionStrict(Keys.LEFT_DRAWER_ACTION),
            rightDrawerAction = getDrawerActionStrict(Keys.RIGHT_DRAWER_ACTION),
            leftDrawerWidth = getIntStrict(Keys.LEFT_DRAWER_WIDTH),
            rightDrawerWidth = getIntStrict(Keys.RIGHT_DRAWER_WIDTH)
        )

        ctx.drawerDataStore.edit { prefs ->
            prefs[AUTO_OPEN_SINGLE_MATCH] = backup.autoOpenSingleMatch
            prefs[SHOW_APP_ICONS_IN_DRAWER] = backup.showAppIconsInDrawer
            prefs[SHOW_APP_LABEL_IN_DRAWER] = backup.showAppLabelInDrawer
            prefs[SEARCH_BAR_BOTTOM] = backup.searchBarBottom
            prefs[AUTO_SHOW_KEYBOARD_ON_DRAWER] = backup.autoShowKeyboardOnDrawer
            prefs[CLICK_EMPTY_SPACE_TO_RAISE_KEYBOARD] = backup.clickEmptySpaceToRaiseKeyboard
            prefs[GRID_SIZE] = backup.gridSize
            prefs[INITIAL_PAGE] = backup.initialPage
            prefs[LEFT_DRAWER_ACTION] = backup.leftDrawerAction.name
            prefs[RIGHT_DRAWER_ACTION] = backup.rightDrawerAction.name
            prefs[LEFT_DRAWER_WIDTH] = backup.leftDrawerWidth
            prefs[RIGHT_DRAWER_WIDTH] = backup.rightDrawerWidth
        }
    }
}
