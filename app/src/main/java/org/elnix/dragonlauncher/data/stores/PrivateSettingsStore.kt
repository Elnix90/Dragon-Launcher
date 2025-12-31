package org.elnix.dragonlauncher.data.stores


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.getIntStrict
import org.elnix.dragonlauncher.data.privateSettingsStore
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore.Keys.ALL
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore.Keys.HAS_INITIALIZED
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore.Keys.HAS_SEEN_WELCOME
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore.Keys.LAST_SEEN_VERSION_CODE
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore.Keys.SHOW_METHOD_ASKING
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore.Keys.SHOW_SET_DEFAULT_LAUNCHER_BANNER
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore.Keys.USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT
import org.elnix.dragonlauncher.data.uiDatastore

object PrivateSettingsStore : BaseSettingsStore<Map<String, Any?>>() {
    override val name: String = "Private"

    // ---------------------------------------------------------
    // Backup structure
    // ---------------------------------------------------------
    private data class PrivateSettingsBackup(
        val hasSeenWelcome: Boolean = false,
        val hasInitialized: Boolean = false,
        val showSetDefaultLauncherBanner: Boolean = true,
        val useAccessibilityInsteadOfContextToExpandActionPanel: Boolean = true,
        val showMethodAsking: Boolean = true,
        val lastSeenVersionCode: Int = 0
    )

    private val defaults = PrivateSettingsBackup()


    private object Keys {

        val HAS_SEEN_WELCOME =
            booleanPreferencesKey("hasSeenWelcome")

        val HAS_INITIALIZED =
            booleanPreferencesKey("hasInitialized")

        val SHOW_SET_DEFAULT_LAUNCHER_BANNER =
            booleanPreferencesKey("showSetDefaultLauncherBanner")

        val USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT =
            booleanPreferencesKey("useAccessibilityInsteadOfContextToExpandActionPanel")

        val SHOW_METHOD_ASKING =
            booleanPreferencesKey("showMethodAsking")

        val LAST_SEEN_VERSION_CODE =
            intPreferencesKey("lastSeenVersionCode")

        val ALL = listOf(
            HAS_SEEN_WELCOME,
            HAS_INITIALIZED,
            SHOW_SET_DEFAULT_LAUNCHER_BANNER,
            USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT,
            SHOW_METHOD_ASKING,
            LAST_SEEN_VERSION_CODE
        )
    }



    // ---------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------
    fun getHasSeenWelcome(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[HAS_SEEN_WELCOME] ?: defaults.hasSeenWelcome
        }

    suspend fun setHasSeenWelcome(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[HAS_SEEN_WELCOME] = v }
    }

    fun getHasInitialized(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[HAS_INITIALIZED] ?: defaults.hasInitialized
        }

    suspend fun setHasInitialized(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[HAS_INITIALIZED] = v }
    }

    fun getShowSetDefaultLauncherBanner(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[SHOW_SET_DEFAULT_LAUNCHER_BANNER] ?: defaults.showSetDefaultLauncherBanner
        }

    suspend fun setShowSetDefaultLauncherBanner(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[SHOW_SET_DEFAULT_LAUNCHER_BANNER] = v }
    }

    fun getUseAccessibilityInsteadOfContextToExpandActionPanel(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] ?: defaults.useAccessibilityInsteadOfContextToExpandActionPanel
        }

    suspend fun setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] = v }
    }

    fun getShowMethodAsking(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[SHOW_METHOD_ASKING] ?: defaults.showMethodAsking
        }

    suspend fun setShowMethodAsking(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[SHOW_METHOD_ASKING] = v }
    }

    fun getLastSeenVersionCode(ctx: Context): Flow<Int> =
        ctx.uiDatastore.data.map { it[LAST_SEEN_VERSION_CODE] ?: defaults.lastSeenVersionCode }

    suspend fun setLastSeenVersionCode(ctx: Context, value: Int) {
        ctx.uiDatastore.edit { it[LAST_SEEN_VERSION_CODE] = value }
    }


    override suspend fun resetAll(ctx: Context) {
        ctx.privateSettingsStore.edit { prefs ->
            ALL.forEach { prefs.remove(it) }
        }
    }


    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.privateSettingsStore.data.first()

        return buildMap {
            putIfNonDefault(
                HAS_SEEN_WELCOME,
                prefs[HAS_SEEN_WELCOME],
                defaults.hasSeenWelcome
            )
            putIfNonDefault(
                HAS_INITIALIZED,
                prefs[HAS_INITIALIZED],
                defaults.hasInitialized
            )
            putIfNonDefault(
                SHOW_SET_DEFAULT_LAUNCHER_BANNER,
                prefs[SHOW_SET_DEFAULT_LAUNCHER_BANNER],
                defaults.showSetDefaultLauncherBanner
            )
            putIfNonDefault(
                USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT,
                prefs[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT],
                defaults.useAccessibilityInsteadOfContextToExpandActionPanel
            )
            putIfNonDefault(
                SHOW_METHOD_ASKING,
                prefs[SHOW_METHOD_ASKING],
                defaults.showMethodAsking
            )
            putIfNonDefault(
                LAST_SEEN_VERSION_CODE,
                prefs[LAST_SEEN_VERSION_CODE],
                defaults.lastSeenVersionCode
            )
        }
    }

    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {
        ctx.privateSettingsStore.edit { prefs ->

            value[HAS_SEEN_WELCOME.name]?.let {
                prefs[HAS_SEEN_WELCOME] =
                    getBooleanStrict(value, HAS_SEEN_WELCOME, defaults.hasSeenWelcome)
            }

            value[HAS_INITIALIZED.name]?.let {
                prefs[HAS_INITIALIZED] =
                    getBooleanStrict(value, HAS_INITIALIZED, defaults.hasInitialized)
            }

            value[SHOW_SET_DEFAULT_LAUNCHER_BANNER.name]?.let {
                prefs[SHOW_SET_DEFAULT_LAUNCHER_BANNER] =
                    getBooleanStrict(
                        value,
                        SHOW_SET_DEFAULT_LAUNCHER_BANNER,
                        defaults.showSetDefaultLauncherBanner
                    )
            }

            value[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT.name]?.let {
                prefs[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] =
                    getBooleanStrict(
                        value,
                        USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT,
                        defaults.useAccessibilityInsteadOfContextToExpandActionPanel
                    )
            }

            value[SHOW_METHOD_ASKING.name]?.let {
                prefs[SHOW_METHOD_ASKING] =
                    getBooleanStrict(
                        value,
                        SHOW_METHOD_ASKING,
                        defaults.showMethodAsking
                    )
            }

            value[LAST_SEEN_VERSION_CODE.name]?.let {
                prefs[LAST_SEEN_VERSION_CODE] =
                    getIntStrict(
                        value,
                        LAST_SEEN_VERSION_CODE,
                        defaults.lastSeenVersionCode
                    )
            }
        }
    }
}
