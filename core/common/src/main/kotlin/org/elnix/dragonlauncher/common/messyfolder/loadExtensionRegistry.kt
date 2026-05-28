package org.elnix.dragonlauncher.common.messyfolder

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.elnix.dragonlauncher.common.serializables.DragonJson
import org.elnix.dragonlauncher.common.serializables.ExtensionModel

@OptIn(ExperimentalSerializationApi::class)
suspend fun loadExtensionRegistry(ctx: Context): List<ExtensionModel>? = withContext(Dispatchers.IO) {
    try {
        ctx.assets.open("extensions-registry.json").use { inputStream ->
            Json.decodeFromStream<List<ExtensionModel>>(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
