package org.elnix.dragonlauncher.settings

import io.github.elnix90.core.stores.SettingsStore

fun Set<String>.toSettingsStoreList(): Set<SettingsStore<*, *>> {
    return this.mapNotNullTo(mutableSetOf()) { storeName ->
        AllStores.find { it.name == storeName }
    }
}
