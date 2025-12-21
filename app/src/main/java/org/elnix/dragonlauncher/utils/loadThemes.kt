package org.elnix.dragonlauncher.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.ThemeObject
import org.json.JSONObject

suspend fun loadThemes(
    ctx: Context
): List<ThemeObject> = withContext(Dispatchers.IO) {

    val am = ctx.assets
    val themesDir = "themes"

    val filesInDir = am.list(themesDir)?.filter { it.endsWith(".json") }.orEmpty()

    val themesList = mutableListOf<ThemeObject>()

    filesInDir.forEach { fileName ->
        try {
            val jsonString = am.open("$themesDir/$fileName")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)

            val themeName = fileName
                .removeSuffix(".json")
                .replace(Regex("[-_]"), " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }

            // Get drawable ID (NOT Painter)
            val imageResId = findThemeImageResource(ctx, themeName.lowercase())

            themesList.add(ThemeObject(
                name = themeName,
                json = jsonObject,
                imageResId = imageResId
            ))
        } catch (e: Exception) {
            println("Failed to load theme $fileName: ${e.message}")
        }
    }

    themesList
}

private fun findThemeImageResource(ctx: Context, themeName: String): Int {
    val imageExtensions = listOf("png", "jpg", "jpeg", "webp")

    println(themeName)
    for (ext in imageExtensions) {
        val imageResId = ctx.resources.getIdentifier(
            "theme_${themeName}",
            "drawable",
            ctx.packageName
        )
        println(imageResId)
        if (imageResId != 0) {
            return imageResId
        }
    }

    return R.drawable.ic_app_default
}
