package org.elnix.dragonlauncher.base.model.models

import androidx.compose.runtime.Immutable
import org.json.JSONObject

@Immutable
public data class ThemeObject(
    val name: String,
    val json: JSONObject,
    val imageAssetPath: String?
)
