package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ExtensionModel")
data class ExtensionModel(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("package") val packageName: String?,
    @SerialName("version") val version: String?,
    @SerialName("descriptions") val description: Map<String, String>,
    @SerialName("author") val author: String?,
    @SerialName("license") val license: String?,
    @SerialName("url") val url: String?,
    @SerialName("download_url") val downloadUrl: String,
    @SerialName("additional_permissions") val permissions: List<String> = emptyList()
)