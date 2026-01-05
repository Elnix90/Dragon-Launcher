package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.SwipeJson
import org.elnix.dragonlauncher.data.behaviorDataStore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.getIntStrict
import org.elnix.dragonlauncher.data.getStringStrict
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore.Keys.BACK_ACTION
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore.Keys.DOUBLE_CLICK_ACTION
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore.Keys.DOWN_PADDING
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore.Keys.KEEP_SCREEN_ON
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore.Keys.LEFT_PADDING
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore.Keys.RIGHT_PADDING
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore.Keys.UP_PADDING

object BehaviorSettingsStore : BaseSettingsStore<Map<String, Any?>>() {
    override val name: String = "Behavior"

    private data class UiSettingsBackup(
        val backAction: SwipeActionSerializable? = null,
        val doubleClickAction: SwipeActionSerializable? = null,
        val keepScreenOn: Boolean = false,
        val leftPadding: Int = 60,
        val rightPadding: Int = 60,
        val upPadding: Int = 80,
        val downPadding: Int = 100,
    )

    private val defaults = UiSettingsBackup()

    private object Keys {
        val BACK_ACTION = stringPreferencesKey("backAction")
        val DOUBLE_CLICK_ACTION = stringPreferencesKey("doubleClickAction")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keepScreenOn")
        val LEFT_PADDING = intPreferencesKey("leftPadding")
        val RIGHT_PADDING = intPreferencesKey("rightPadding")
        val UP_PADDING = intPreferencesKey("upPadding")
        val DOWN_PADDING = intPreferencesKey("downPadding")
        val ALL = listOf(
            BACK_ACTION,
            DOUBLE_CLICK_ACTION,
            KEEP_SCREEN_ON,
            LEFT_PADDING,
            RIGHT_PADDING,
            UP_PADDING,
            DOWN_PADDING
        )
    }

    fun getBackAction(ctx: Context): Flow<SwipeActionSerializable?> =
        ctx.behaviorDataStore.data.map {
            it[BACK_ACTION]?.takeIf { s -> s.isNotBlank() }?.let(SwipeJson::decodeAction)
        }

    fun getDoubleClickAction(ctx: Context): Flow<SwipeActionSerializable?> =
        ctx.behaviorDataStore.data.map {
            it[DOUBLE_CLICK_ACTION]?.takeIf { s -> s.isNotBlank() }?.let(SwipeJson::decodeAction)
        }

    suspend fun setBackAction(ctx: Context, value: SwipeActionSerializable?) {
        ctx.behaviorDataStore.edit {
            if (value != null) it[BACK_ACTION] = SwipeJson.encodeAction(value)
            else it.remove(BACK_ACTION)
        }
    }

    suspend fun setDoubleClickAction(ctx: Context, value: SwipeActionSerializable?) {
        ctx.behaviorDataStore.edit {
            if (value != null) it[DOUBLE_CLICK_ACTION] = SwipeJson.encodeAction(value)
            else it.remove(DOUBLE_CLICK_ACTION)
        }
    }

    fun getKeepScreenOn(ctx: Context): Flow<Boolean> =
        ctx.behaviorDataStore.data.map { it[KEEP_SCREEN_ON] ?: defaults.keepScreenOn }

    suspend fun setKeepScreenOn(ctx: Context, value: Boolean) {
        ctx.behaviorDataStore.edit { it[KEEP_SCREEN_ON] = value }
    }

    suspend fun setLeftPadding(ctx: Context, value: Int) {
        ctx.behaviorDataStore.edit { it[LEFT_PADDING] = value }
    }

    fun getLeftPadding(ctx: Context): Flow<Int> =
        ctx.behaviorDataStore.data.map { it[LEFT_PADDING] ?: defaults.leftPadding }

    suspend fun setRightPadding(ctx: Context, value: Int) {
        ctx.behaviorDataStore.edit { it[RIGHT_PADDING] = value }
    }

    fun getRightPadding(ctx: Context): Flow<Int> =
        ctx.behaviorDataStore.data.map { it[RIGHT_PADDING] ?: defaults.rightPadding }

    suspend fun setUpPadding(ctx: Context, value: Int) {
        ctx.behaviorDataStore.edit { it[UP_PADDING] = value }
    }

    fun getUpPadding(ctx: Context): Flow<Int> =
        ctx.behaviorDataStore.data.map { it[UP_PADDING] ?: defaults.upPadding }

    suspend fun setDownPadding(ctx: Context, value: Int) {
        ctx.behaviorDataStore.edit { it[DOWN_PADDING] = value }
    }

    fun getDownPadding(ctx: Context): Flow<Int> =
        ctx.behaviorDataStore.data.map { it[DOWN_PADDING] ?: defaults.downPadding }


    override suspend fun resetAll(ctx: Context) {
        ctx.behaviorDataStore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.behaviorDataStore.data.first()

        return buildMap {

            putIfNonDefault(
                BACK_ACTION,
                prefs[BACK_ACTION],
                defaults.backAction
            )

            putIfNonDefault(
                DOUBLE_CLICK_ACTION,
                prefs[DOUBLE_CLICK_ACTION],
                defaults.doubleClickAction
            )

            putIfNonDefault(
                KEEP_SCREEN_ON,
                prefs[KEEP_SCREEN_ON],
                defaults.keepScreenOn
            )

            putIfNonDefault(
                LEFT_PADDING,
                prefs[LEFT_PADDING],
                defaults.leftPadding
            )

            putIfNonDefault(
                RIGHT_PADDING,
                prefs[RIGHT_PADDING],
                defaults.rightPadding
            )

            putIfNonDefault(
                UP_PADDING,
                prefs[UP_PADDING],
                defaults.upPadding
            )

            putIfNonDefault(
                DOWN_PADDING,
                prefs[DOWN_PADDING],
                defaults.downPadding
            )
        }
    }


    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {
        ctx.behaviorDataStore.edit { prefs ->

            prefs[BACK_ACTION] =
                getStringStrict(
                    value,
                    BACK_ACTION,
                    defaults.backAction.toString()
                )

            prefs[DOUBLE_CLICK_ACTION] =
                getStringStrict(
                    value,
                    DOUBLE_CLICK_ACTION,
                    defaults.doubleClickAction.toString()
                )

            prefs[KEEP_SCREEN_ON] =
                getBooleanStrict(
                    value,
                    KEEP_SCREEN_ON,
                    defaults.keepScreenOn
                )

            prefs[LEFT_PADDING] =
                getIntStrict(
                    value,
                    LEFT_PADDING,
                    defaults.leftPadding
                )

            prefs[RIGHT_PADDING] =
                getIntStrict(
                    value,
                    RIGHT_PADDING,
                    defaults.rightPadding
                )

            prefs[UP_PADDING] =
                getIntStrict(
                    value,
                    UP_PADDING,
                    defaults.upPadding
                )

            prefs[DOWN_PADDING] =
                getIntStrict(
                    value,
                    DOWN_PADDING,
                    defaults.downPadding
                )
        }
    }
}
