package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string

object LanguageSettingsStore : MapSettingsStore(DataStoreName.LANGUAGE) {
    override val ALL: List<BaseSettingObject<*,*>>
        get() = listOf(this.keyLang)

    val keyLang = string(
        key = "pref_app_language",
        default = ""
    )
}
