package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.getIntStrict
import org.elnix.dragonlauncher.data.getStringStrict
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.statusBarDatastore
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.BAR_BACKGROUND_COLOR
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.BAR_TEXT_COLOR
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.DATE_FORMATTER
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.SHOW_BATTERY
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.SHOW_CONNECTIVITY
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.SHOW_DATE
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.SHOW_NEXT_ALARM
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.SHOW_NOTIFICATIONS
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.SHOW_STATUS_BAR
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.SHOW_TIME
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore.Keys.TIME_FORMATTER
import org.elnix.dragonlauncher.ui.theme.AmoledDefault

object StatusBarSettingsStore : BaseSettingsStore<Map<String, Any?>>() {

    override val name: String = "Status Bar"

    private data class SettingsBackup(
        val showStatusBar: Boolean = true,
        val barBackgroundColor: Int = Color.Transparent.toArgb(),
        val barTextColor: Int = AmoledDefault.OnBackground.toArgb(),
        val showTime: Boolean = true,
        val showDate: Boolean = false,
        val timeFormatter: String = "HH:mm:ss",
        val dateFormatter: String = "MMM dd",
        val showNotifications: Boolean = false,
        val showBattery: Boolean = true,
        val showConnectivity: Boolean = false,
        val showNextAlarm: Boolean = true,
        val leftPadding: Int = 0,
        val rightPadding: Int = 0
    )

    private val defaults = SettingsBackup()

    private object Keys {
        val SHOW_STATUS_BAR = booleanPreferencesKey(SettingsBackup::showStatusBar.name)
        val BAR_BACKGROUND_COLOR = intPreferencesKey(SettingsBackup::barBackgroundColor.name)
        val BAR_TEXT_COLOR = intPreferencesKey(SettingsBackup::barTextColor.name)
        val SHOW_TIME = booleanPreferencesKey(SettingsBackup::showTime.name)
        val SHOW_DATE = booleanPreferencesKey(SettingsBackup::showDate.name)
        val TIME_FORMATTER = stringPreferencesKey("timeFormatter")
        val DATE_FORMATTER = stringPreferencesKey("dateFormatter")
        val SHOW_NOTIFICATIONS = booleanPreferencesKey(SettingsBackup::showNotifications.name)
        val SHOW_BATTERY = booleanPreferencesKey(SettingsBackup::showBattery.name)
        val SHOW_CONNECTIVITY = booleanPreferencesKey(SettingsBackup::showConnectivity.name)
        val SHOW_NEXT_ALARM = booleanPreferencesKey("showNextAlarm")
        val LEFT_PADDING = intPreferencesKey(SettingsBackup::leftPadding.name)
        val RIGHT_PADDING = intPreferencesKey(SettingsBackup::rightPadding.name)

        val ALL = listOf(
            SHOW_STATUS_BAR,
            BAR_BACKGROUND_COLOR,
            BAR_TEXT_COLOR,
            SHOW_TIME,
            SHOW_DATE,
            TIME_FORMATTER,
            DATE_FORMATTER,
            SHOW_NOTIFICATIONS,
            SHOW_BATTERY,
            SHOW_CONNECTIVITY,
            SHOW_NEXT_ALARM,
            LEFT_PADDING,
            RIGHT_PADDING
        )
    }

    /* ───────────── visibility ───────────── */

    fun getShowStatusBar(ctx: Context): Flow<Boolean> =
        ctx.statusBarDatastore.data.map { it[SHOW_STATUS_BAR] ?: defaults.showStatusBar }

    suspend fun setShowStatusBar(ctx: Context, value: Boolean) {
        ctx.statusBarDatastore.edit { it[SHOW_STATUS_BAR] = value }
    }

    /* ───────────── colors ───────────── */

    fun getBarBackgroundColor(ctx: Context): Flow<Color> =
        ctx.statusBarDatastore.data.map {
            Color(it[BAR_BACKGROUND_COLOR] ?: defaults.barBackgroundColor)
        }

    suspend fun setBarBackgroundColor(ctx: Context, color: Color) {
        ctx.statusBarDatastore.edit {
            it[BAR_BACKGROUND_COLOR] = color.toArgb()
        }
    }

    fun getBarTextColor(ctx: Context): Flow<Color> =
        ctx.statusBarDatastore.data.map {
            Color(it[BAR_TEXT_COLOR] ?: defaults.barTextColor)
        }

    suspend fun setBarTextColor(ctx: Context, color: Color) {
        ctx.statusBarDatastore.edit {
            it[BAR_TEXT_COLOR] = color.toArgb()
        }
    }

    /* ───────────── existing flags ───────────── */

    fun getShowTime(ctx: Context): Flow<Boolean> =
        ctx.statusBarDatastore.data.map { it[SHOW_TIME] ?: defaults.showTime }

    suspend fun setShowTime(ctx: Context, value: Boolean) {
        ctx.statusBarDatastore.edit { it[SHOW_TIME] = value }
    }

    fun getShowDate(ctx: Context): Flow<Boolean> =
        ctx.statusBarDatastore.data.map { it[SHOW_DATE] ?: defaults.showDate }

    suspend fun setShowDate(ctx: Context, value: Boolean) {
        ctx.statusBarDatastore.edit { it[SHOW_DATE] = value }
    }


    fun getTimeFormatter(ctx: Context): Flow<String> =
        ctx.statusBarDatastore.data.map {
            it[TIME_FORMATTER] ?: defaults.timeFormatter
        }

    suspend fun setTimeFormatter(ctx: Context, formatter: String) {
        ctx.statusBarDatastore.edit { it[TIME_FORMATTER] = formatter }
    }

    fun getDateFormatter(ctx: Context): Flow<String> =
        ctx.statusBarDatastore.data.map {
            it[DATE_FORMATTER] ?: defaults.dateFormatter
        }

    suspend fun setDateFormatter(ctx: Context, formatter: String) {
        ctx.statusBarDatastore.edit { it[DATE_FORMATTER] = formatter }
    }

    fun getShowNotifications(ctx: Context): Flow<Boolean> =
        ctx.statusBarDatastore.data.map { it[SHOW_NOTIFICATIONS] ?: defaults.showNotifications }

    suspend fun setShowNotifications(ctx: Context, value: Boolean) {
        ctx.statusBarDatastore.edit { it[SHOW_NOTIFICATIONS] = value }
    }

    fun getShowBattery(ctx: Context): Flow<Boolean> =
        ctx.statusBarDatastore.data.map { it[SHOW_BATTERY] ?: defaults.showBattery }

    suspend fun setShowBattery(ctx: Context, value: Boolean) {
        ctx.statusBarDatastore.edit { it[SHOW_BATTERY] = value }
    }

    fun getShowConnectivity(ctx: Context): Flow<Boolean> =
        ctx.statusBarDatastore.data.map { it[SHOW_CONNECTIVITY] ?: defaults.showConnectivity }

    suspend fun setShowConnectivity(ctx: Context, value: Boolean) {
        ctx.statusBarDatastore.edit { it[SHOW_CONNECTIVITY] = value }
    }

    fun getShowNextAlarm(ctx: Context): Flow<Boolean> =
        ctx.statusBarDatastore.data.map { it[SHOW_NEXT_ALARM] ?: defaults.showNextAlarm }

    suspend fun setShowNextAlarm(ctx: Context, value: Boolean) {
        ctx.statusBarDatastore.edit { it[SHOW_NEXT_ALARM] = value }
    }

    fun getLeftPadding(ctx: Context): Flow<Int> =
        ctx.statusBarDatastore.data.map { it[Keys.LEFT_PADDING] ?: defaults.leftPadding }

    suspend fun setLeftPadding(ctx: Context, value: Int) {
        ctx.statusBarDatastore.edit { it[Keys.LEFT_PADDING] = value }
    }

    fun getRightPadding(ctx: Context): Flow<Int> =
        ctx.statusBarDatastore.data.map { it[Keys.RIGHT_PADDING] ?: defaults.rightPadding }

    suspend fun setRightPadding(ctx: Context, value: Int) {
        ctx.statusBarDatastore.edit { it[Keys.RIGHT_PADDING] = value }
    }

    /* ───────────── reset / backup ───────────── */

    override suspend fun resetAll(ctx: Context) {
        ctx.statusBarDatastore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }


    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.statusBarDatastore.data.first()

        return buildMap {
            putIfNonDefault(SHOW_STATUS_BAR, prefs[SHOW_STATUS_BAR], defaults.showStatusBar)
            putIfNonDefault(BAR_BACKGROUND_COLOR, prefs[BAR_BACKGROUND_COLOR], defaults.barBackgroundColor)
            putIfNonDefault(BAR_TEXT_COLOR, prefs[BAR_TEXT_COLOR], defaults.barTextColor)
            putIfNonDefault(SHOW_TIME, prefs[SHOW_TIME], defaults.showTime)
            putIfNonDefault(SHOW_DATE, prefs[SHOW_DATE], defaults.showDate)
            putIfNonDefault(TIME_FORMATTER, prefs[TIME_FORMATTER], defaults.timeFormatter)
            putIfNonDefault(DATE_FORMATTER, prefs[DATE_FORMATTER], defaults.dateFormatter)
            putIfNonDefault(SHOW_NOTIFICATIONS, prefs[SHOW_NOTIFICATIONS], defaults.showNotifications)
            putIfNonDefault(SHOW_BATTERY, prefs[SHOW_BATTERY], defaults.showBattery)
            putIfNonDefault(SHOW_CONNECTIVITY, prefs[SHOW_CONNECTIVITY], defaults.showConnectivity)
            putIfNonDefault(SHOW_NEXT_ALARM, prefs[SHOW_NEXT_ALARM], defaults.showNextAlarm)
            putIfNonDefault(Keys.LEFT_PADDING, prefs[Keys.LEFT_PADDING], defaults.leftPadding)
            putIfNonDefault(Keys.RIGHT_PADDING, prefs[Keys.RIGHT_PADDING], defaults.rightPadding)
        }
    }

    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {
        ctx.statusBarDatastore.edit { prefs ->
            prefs[SHOW_STATUS_BAR] =
                getBooleanStrict(value, SHOW_STATUS_BAR, defaults.showStatusBar)

            prefs[BAR_BACKGROUND_COLOR] =
                getIntStrict(value, BAR_BACKGROUND_COLOR, defaults.barBackgroundColor)

            prefs[BAR_TEXT_COLOR] =
                getIntStrict(value, BAR_TEXT_COLOR, defaults.barTextColor)

            prefs[SHOW_TIME] =
                getBooleanStrict(value, SHOW_TIME, defaults.showTime)

            prefs[SHOW_DATE] =
                getBooleanStrict(value, SHOW_DATE, defaults.showDate)

            prefs[TIME_FORMATTER] =
                getStringStrict(value, TIME_FORMATTER, defaults.timeFormatter)

            prefs[DATE_FORMATTER] =
                getStringStrict(value, DATE_FORMATTER, defaults.dateFormatter)

            prefs[SHOW_NOTIFICATIONS] =
                getBooleanStrict(value, SHOW_NOTIFICATIONS, defaults.showNotifications)

            prefs[SHOW_BATTERY] =
                getBooleanStrict(value, SHOW_BATTERY, defaults.showBattery)

            prefs[SHOW_CONNECTIVITY] =
                getBooleanStrict(value, SHOW_CONNECTIVITY, defaults.showConnectivity)

            prefs[SHOW_NEXT_ALARM] =
                getBooleanStrict(value, SHOW_NEXT_ALARM, defaults.showNextAlarm)

            prefs[Keys.LEFT_PADDING] =
                getIntStrict(value, Keys.LEFT_PADDING, defaults.leftPadding)

            prefs[Keys.RIGHT_PADDING] =
                getIntStrict(value, Keys.RIGHT_PADDING, defaults.rightPadding)
        }
    }
}
