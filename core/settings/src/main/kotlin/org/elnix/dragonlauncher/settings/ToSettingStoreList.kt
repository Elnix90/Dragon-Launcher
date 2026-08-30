package org.elnix.dragonlauncher.settings

import io.github.elnix90.core.stores.SettingsStore

public fun Set<String>.toSettingsStoreList(): Set<SettingsStore<*, *>> =
    this.mapNotNullTo(
        mutableSetOf()
    ) { storeName ->
        AllStores.find { it.name == storeName }
    }
