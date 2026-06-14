package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.settings.SettingKey
import org.elnix.settings.SettingStore

@SettingStore
object LanguageSettingsStore : MapSettingsStore(DataStoreName.LANGUAGE) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(this.keyLang)
    }

    @SettingKey
    val keyLang = string(
        title = null,
        description = null,
        default = "",
    )
}