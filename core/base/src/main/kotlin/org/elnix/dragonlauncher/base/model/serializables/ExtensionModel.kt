package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ExtensionModel")
public data class ExtensionModel(
    val id: String,
    val name: String,
    val packageName: String?,
    val version: String?,
    val description: Map<String, String>,
    val author: String?,
    val license: String?,
    val url: String?,
    val downloadUrl: String,
    val permissions: List<String> = emptyList()
)