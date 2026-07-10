package org.elnix.dragonlauncher.common.loader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.Constants.Paths.THEMES_DIR
import org.elnix.dragonlauncher.base.Constants.Paths.imageExts
import org.elnix.dragonlauncher.base.model.models.ThemeObject
import org.json.JSONObject


// TODO Move to themes service

public suspend fun loadThemes(ctx: Context): List<ThemeObject> = withContext(Dispatchers.IO) {
    val am = ctx.assets
    val jsonFiles = am.list(THEMES_DIR)?.filter { it.endsWith(".json") }.orEmpty()
    val themesList = mutableListOf<ThemeObject>()

    jsonFiles.forEach { jsonFileName ->
        try {
            val jsonString = am.open("${THEMES_DIR}/$jsonFileName").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val themeBaseName = jsonFileName.removeSuffix(".json")
            val themeName = themeBaseName.replace(Regex("[-_]"), " ")
                .split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }

            // Find exact matching image
            val imageAssetPath = imageExts.firstOrNull { ext ->
                val imageFile = "${themeBaseName}.$ext"
                am.list(THEMES_DIR)?.contains(imageFile) == true
            }?.let { ext -> "${THEMES_DIR}/${themeBaseName}.$ext" }

            themesList.add(
                ThemeObject(
                    name = themeName,
                    json = jsonObject,
                    imageAssetPath = imageAssetPath
                )
            )
        } catch (e: Exception) {
            println("Failed to load theme $jsonFileName")
        }
    }
    themesList
}
