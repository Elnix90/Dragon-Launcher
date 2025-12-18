package org.elnix.dragonlauncher.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.ui.whatsnew.Update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

suspend fun loadChangelogs(
    context: Context,
    currentVersionCode: Int
): List<Update> = withContext(Dispatchers.IO) {

    val am = context.assets
    val changelogDir = "changelogs"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val filesInDir = am.list(changelogDir).orEmpty()

    val versionFiles = filesInDir
        .filter { it.matches(Regex("\\d+\\.txt")) }
        .mapNotNull { file ->
            file.removeSuffix(".txt").toIntOrNull()?.let { it to file }
        }
        .filter { it.first <= currentVersionCode }
        .sortedByDescending { it.first }

    versionFiles.mapNotNull { (versionCode, filename) ->
        try {
            val lines = am.open("$changelogDir/$filename")
                .bufferedReader()
                .readLines()
                .map { it.trim() }

            if (lines.size < 2) return@mapNotNull null

            val versionName = lines[0]
            val date = runCatching {
                dateFormat.parse(lines[1])
            }.getOrElse { Date(0) }

            val whatsNew = mutableListOf<String>()
            val improved = mutableListOf<String>()
            val fixed = mutableListOf<String>()
            val knownIssues = mutableListOf<String>()

            var currentSection: MutableList<String>? = null

            lines.drop(2).forEach { line ->
                when (line) {
                    "[NEW]" -> currentSection = whatsNew
                    "[IMPROVED]" -> currentSection = improved
                    "[FIXED]" -> currentSection = fixed
                    "[ISSUES]" -> currentSection = knownIssues
                    else -> {
                        if (line.startsWith("* ")) {
                            currentSection?.add(
                                line.removePrefix("* ").trim()
                            )
                        }
                    }
                }
            }

            Update(
                versionCode = versionCode,
                versionName = versionName,
                date = date,
                whatsNew = whatsNew.takeIf { it.isNotEmpty() },
                improved = improved.takeIf { it.isNotEmpty() },
                fixed = fixed.takeIf { it.isNotEmpty() },
                knownIssues = knownIssues.takeIf { it.isNotEmpty() }
            )

        } catch (e: Exception) {
            Log.e("Changelogs", "Failed to parse $filename", e)
            null
        }
    }
}
