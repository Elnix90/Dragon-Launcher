package org.elnix.dragonlauncher.base

import android.content.Context
import org.elnix.dragonlauncher.EXTENSION_MANAGER_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.model.serializables.ExtensionModel


// TODO Move to extensions service

@OptIn(ExperimentalSerializationApi::class)
public suspend fun loadExtensionRegistry(ctx: Context): List<ExtensionModel>? = withContext(Dispatchers.IO) {
    try {
        ctx.assets.open("extensions-registry.json").use { inputStream ->
            json.decodeFromStream<List<ExtensionModel>>(inputStream)
        }
    } catch (e: Exception) {
        logE(EXTENSION_MANAGER_TAG, e) { "Failed to load extensions" }
        null
    }
}
