package org.elnix.dragonlauncher.common.messyfolder

import org.json.JSONObject

data class ThemeObject(
    val name: String,
    val json: JSONObject,
    val imageAssetPath: String?
)
