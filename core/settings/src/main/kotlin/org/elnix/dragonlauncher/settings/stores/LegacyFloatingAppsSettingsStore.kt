package org.elnix.dragonlauncher.settings.stores

import android.content.Context
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.common.serializables.FloatingAppObject
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipeJson
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.FLOATING_APPS_TAG
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.Settings
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.JsonObjectSettingsStore
import java.util.Collections.emptyList

object LegacyFloatingAppsSettingsStore : JsonObjectSettingsStore() {

    override val name: String = "Legacy Floating Apps"
    override val dataStoreName: DataStoreName
        get() = DataStoreName.LEGACY_FLOATING_APPS


    override val jsonSetting = Settings.string(
        key = "floating_apps",
        dataStoreName = dataStoreName,
        default = ""
    )

    override val ALL: List<BaseSettingObject<*,*>>
        get() = listOf(this.jsonSetting)

    suspend fun legacyLoadFloatingApps(ctx: Context): List<FloatingAppObject> {
        return try {
            val allJson = getAll(ctx)
            logD(FLOATING_APPS_TAG) { "Legacy raw: $allJson" }
            val floatingAppArray = allJson?.optJSONArray("floating_apps") ?: return emptyList()

            val floatingApps = mutableListOf<FloatingAppObject>()
            for (i in 0 until floatingAppArray.length()) {
                val obj = floatingAppArray.getJSONObject(i)

                floatingApps.add(
                    FloatingAppObject(
                        id = obj.getInt("id"),
                        appWidgetId = if (obj.has("appWidgetId")) obj.getInt("appWidgetId") else null,
                        nestId = obj.getInt("nestId"),
                        action = SwipeJson.decodeAction(obj.getString("action"))
                            ?: SwipeActionSerializable.LaunchApp(ctx.packageName, false, 0),
                        spanX = obj.optDouble("spanX", 1.0).toFloat(),
                        spanY = obj.optDouble("spanY", 1.0).toFloat(),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        angle = obj.optDouble("angle", 0.0).toFloat(),
                        ghosted = obj.optBoolean("ghosted", false),
                        foreground = obj.optBoolean("foreground", true)
                    )
                )
            }
            logD(FLOATING_APPS_TAG) { "Legacy loaded ${floatingApps.size} floatingApps" }
            floatingApps
        } catch (e: Exception) {
            logE(FLOATING_APPS_TAG, e) { "Legacy load failed" }
            emptyList()
        }
    }
}