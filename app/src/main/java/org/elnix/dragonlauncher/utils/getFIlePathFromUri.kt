package org.elnix.dragonlauncher.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns

fun getFilePathFromUri(context: Context, uri: Uri): String {
    // 1. Try SAF document path reconstruction
    if (DocumentsContract.isDocumentUri(context, uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":")
        if (split.size == 2) {
            val type = split[0]
            val subPath = split[1]

            // Internal storage (primary)
            if (type.equals("primary", ignoreCase = true)) {
                return "/storage/emulated/0/$subPath"
            }
        }
    }

    // 2. If not from primary storage: fall back to the display name
    val name = getDisplayName(context, uri)
    if (name != null) return name

    // 3. Last fallback: last path segment
    return uri.lastPathSegment ?: "Unknown file"
}

private fun getDisplayName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else null
        }
    } catch (_: Exception) {
        null
    }
}
