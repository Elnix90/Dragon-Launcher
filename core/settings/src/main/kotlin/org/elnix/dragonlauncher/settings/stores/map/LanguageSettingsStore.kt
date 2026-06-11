package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object LanguageSettingsStore : MapSettingsStore(DataStoreName.LANGUAGE) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(this.keyLang)

    val keyLang by string(
        title = null,
        description = null,
        default = ""
    )
}