package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.debugDatastore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.getStringStrict
import org.elnix.dragonlauncher.data.privateSettingsStore
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.DEBUG_ENABLED
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.DEBUG_INFOS
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.FORCE_APP_LANGUAGE_SELECTOR
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.SETTINGS_DEBUG_INFOS
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.SYSTEM_LAUNCHER_PACKAGE_NAME
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.WIDGETS_DEBUG_INFOS
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore.Keys.WORKSPACES_DEBUG_INFO

object DebugSettingsStore : BaseSettingsStore<Map<String, Any?>>() {

    override val name: String = "Debug"

    // -------------------------------------------------------------------------
    // Backup data class
    // -------------------------------------------------------------------------
    private data class DebugSettingsBackup(
        val debugEnabled: Boolean = false,
        val debugInfos: Boolean = false,
        val settingsDebugInfo: Boolean = false,
        val widgetsDebugInfo: Boolean = false,
        val workspacesDebugInfo: Boolean = false,
        val forceAppLanguageSelector: Boolean = false,
        val autoRaiseDragonOnSystemLauncher: Boolean = false,
        val systemLauncherPackageName: String = "",
        val useAccessibilityInsteadOfContextToExpandActionPanel: Boolean = true,
        )

    private val defaults = DebugSettingsBackup()

    // -------------------------------------------------------------------------
    // Keys object for safer reference
    // -------------------------------------------------------------------------
    private object Keys {
        val DEBUG_ENABLED = booleanPreferencesKey(DebugSettingsBackup::debugEnabled.name)
        val DEBUG_INFOS = booleanPreferencesKey(DebugSettingsBackup::debugInfos.name)
        val SETTINGS_DEBUG_INFOS = booleanPreferencesKey(DebugSettingsBackup::settingsDebugInfo.name)
        val WIDGETS_DEBUG_INFOS = booleanPreferencesKey(DebugSettingsBackup::widgetsDebugInfo.name)
        val WORKSPACES_DEBUG_INFO = booleanPreferencesKey(DebugSettingsBackup::workspacesDebugInfo.name)
        val FORCE_APP_LANGUAGE_SELECTOR = booleanPreferencesKey(DebugSettingsBackup::forceAppLanguageSelector.name)
        val AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER = booleanPreferencesKey(DebugSettingsBackup::autoRaiseDragonOnSystemLauncher.name)
        val SYSTEM_LAUNCHER_PACKAGE_NAME = stringPreferencesKey(DebugSettingsBackup::systemLauncherPackageName.name)
        val USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT =
            booleanPreferencesKey(DebugSettingsBackup::useAccessibilityInsteadOfContextToExpandActionPanel.name)

        val ALL = listOf(
            DEBUG_ENABLED,
            DEBUG_INFOS,
            SETTINGS_DEBUG_INFOS,
            WIDGETS_DEBUG_INFOS,
            WORKSPACES_DEBUG_INFO,
            FORCE_APP_LANGUAGE_SELECTOR,
            AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER,
            SYSTEM_LAUNCHER_PACKAGE_NAME,
            USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT
        )
    }


    // -------------------------------------------------------------------------
    // Accessors + Mutators
    // -------------------------------------------------------------------------
    fun getDebugEnabled(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[DEBUG_ENABLED] ?: defaults.debugEnabled
        }

    suspend fun setDebugEnabled(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[DEBUG_ENABLED] = enabled }
    }

    fun getDebugInfos(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[DEBUG_INFOS] ?: defaults.debugInfos
        }

    suspend fun setDebugInfos(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[DEBUG_INFOS] = enabled }
    }

    fun getSettingsDebugInfos(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[SETTINGS_DEBUG_INFOS] ?: defaults.settingsDebugInfo
        }

    suspend fun setSettingsDebugInfos(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[SETTINGS_DEBUG_INFOS] = enabled }
    }

    fun getWidgetsDebugInfos(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[WIDGETS_DEBUG_INFOS] ?: defaults.widgetsDebugInfo
        }

    suspend fun setWidgetsDebugInfos(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[WIDGETS_DEBUG_INFOS] = enabled }
    }

    fun getWorkspacesDebugInfos(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[WORKSPACES_DEBUG_INFO] ?: defaults.workspacesDebugInfo
        }

    suspend fun setWorkspacesDebugInfos(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[WORKSPACES_DEBUG_INFO] = enabled }
    }
    fun getForceAppLanguageSelector(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[FORCE_APP_LANGUAGE_SELECTOR] ?: defaults.forceAppLanguageSelector
        }

    suspend fun setForceAppLanguageSelector(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[FORCE_APP_LANGUAGE_SELECTOR] = enabled }
    }

    fun getAutoRaiseDragonOnSystemLauncher(ctx: Context): Flow<Boolean> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER] ?: defaults.autoRaiseDragonOnSystemLauncher
        }

    suspend fun setAutoRaiseDragonOnSystemLauncher(ctx: Context, enabled: Boolean) {
        ctx.debugDatastore.edit { it[AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER] = enabled }
    }

    fun getSystemLauncherPackageName(ctx: Context): Flow<String> =
        ctx.debugDatastore.data.map { prefs ->
            prefs[SYSTEM_LAUNCHER_PACKAGE_NAME] ?: defaults.systemLauncherPackageName
        }

    suspend fun setSystemLauncherPackageName(ctx: Context, pkg: String?) {
        ctx.debugDatastore.edit { prefs ->
            pkg?.let {
                prefs[SYSTEM_LAUNCHER_PACKAGE_NAME] = it
            } ?: prefs.remove(SYSTEM_LAUNCHER_PACKAGE_NAME)
        }
    }



    fun getUseAccessibilityInsteadOfContextToExpandActionPanel(ctx: Context): Flow<Boolean> =
        ctx.privateSettingsStore.data.map { prefs ->
            prefs[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] ?: defaults.useAccessibilityInsteadOfContextToExpandActionPanel
        }

    suspend fun setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx: Context, v: Boolean) {
        ctx.privateSettingsStore.edit { it[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] = v }
    }



    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------
    override suspend fun resetAll(ctx: Context) {
        ctx.debugDatastore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    // -------------------------------------------------------------------------
    // Backup export
    // -------------------------------------------------------------------------
    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.debugDatastore.data.first()

        return buildMap {
            putIfNonDefault(DEBUG_ENABLED, prefs[DEBUG_ENABLED], defaults.debugEnabled)
            putIfNonDefault(DEBUG_INFOS, prefs[DEBUG_INFOS], defaults.debugInfos)
            putIfNonDefault(SETTINGS_DEBUG_INFOS, prefs[SETTINGS_DEBUG_INFOS], defaults.settingsDebugInfo)
            putIfNonDefault(WIDGETS_DEBUG_INFOS, prefs[WIDGETS_DEBUG_INFOS], defaults.widgetsDebugInfo)
            putIfNonDefault(WORKSPACES_DEBUG_INFO, prefs[WORKSPACES_DEBUG_INFO], defaults.workspacesDebugInfo)
            putIfNonDefault(FORCE_APP_LANGUAGE_SELECTOR, prefs[FORCE_APP_LANGUAGE_SELECTOR], defaults.forceAppLanguageSelector)
            putIfNonDefault(AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER, prefs[AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER], defaults.autoRaiseDragonOnSystemLauncher)
            putIfNonDefault(SYSTEM_LAUNCHER_PACKAGE_NAME, prefs[SYSTEM_LAUNCHER_PACKAGE_NAME], defaults.systemLauncherPackageName)
            putIfNonDefault(
                USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT,
                prefs[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT],
                defaults.useAccessibilityInsteadOfContextToExpandActionPanel
            )
        }
    }

    // -------------------------------------------------------------------------
    // Backup import
    // -------------------------------------------------------------------------
    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {

        ctx.debugDatastore.edit { prefs ->

            prefs[DEBUG_ENABLED] =
                getBooleanStrict(value, DEBUG_ENABLED, defaults.debugEnabled)

            prefs[DEBUG_INFOS] =
                getBooleanStrict(value, DEBUG_INFOS, defaults.debugInfos)

            prefs[SETTINGS_DEBUG_INFOS] =
                getBooleanStrict(value, SETTINGS_DEBUG_INFOS, defaults.settingsDebugInfo)

            prefs[WIDGETS_DEBUG_INFOS] =
                getBooleanStrict(value, WIDGETS_DEBUG_INFOS, defaults.widgetsDebugInfo)

            prefs[WORKSPACES_DEBUG_INFO] =
                getBooleanStrict(value, WORKSPACES_DEBUG_INFO, defaults.workspacesDebugInfo)

            prefs[FORCE_APP_LANGUAGE_SELECTOR] =
                getBooleanStrict(value,FORCE_APP_LANGUAGE_SELECTOR, defaults.forceAppLanguageSelector)

            prefs[AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER] =
                getBooleanStrict(value,AUTO_RAISE_DRAGON_ON_SYSTEM_LAUNCHER, defaults.autoRaiseDragonOnSystemLauncher)

            prefs[SYSTEM_LAUNCHER_PACKAGE_NAME] =
                getStringStrict(value,SYSTEM_LAUNCHER_PACKAGE_NAME, defaults.systemLauncherPackageName)

            value[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT.name]?.let {
                prefs[USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT] =
                    getBooleanStrict(
                        value,
                        USE_ACCESSIBILITY_INSTEAD_OF_CONTEXT,
                        defaults.useAccessibilityInsteadOfContextToExpandActionPanel
                    )
            }
        }
    }
}
