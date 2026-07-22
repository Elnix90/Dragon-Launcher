package org.elnix.dragonlauncher.common.loader

import android.content.Context
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
        e.printStackTrace()
        null
    }
}
