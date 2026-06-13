package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object LanguageSettingsStore : MapSettingsStore(DataStoreName.LANGUAGE) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(this.keyLang)
    }

    val keyLang by lazy {
        StringSettingObject(
            title = null,
            description = null,
            default = "",
            key = "keyLang",
            dataStoreName = DataStoreName.LANGUAGE,
            onChanged = null
        )
    }
}