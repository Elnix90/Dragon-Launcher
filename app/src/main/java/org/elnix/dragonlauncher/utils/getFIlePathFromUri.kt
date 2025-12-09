package org.elnix.dragonlauncher.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

fun getFilePathFromUri(context: Context, uri: Uri): String {
    return when {
        DocumentsContract.isDocumentUri(context, uri) -> {
            val docId = DocumentsContract.getDocumentId(uri)
            val splitId = docId.split(":").toTypedArray()
            if (splitId.size > 1 && splitId[0] == "primary") {
                val pathPart = splitId[1].replace("%20", " ")
                "/storage/emulated/0/$pathPart".trimEnd('/')
            } else {
                docId
            }
        }
        else -> uri.lastPathSegment ?: "Unknown file"
    }
}
