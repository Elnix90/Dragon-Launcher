package org.elnix.dragonlauncher.ui.helpers

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.data.SwipeActionSerializable

@Composable
fun FilePickerDialog(
    onDismiss: () -> Unit,
    onFileSelected: (SwipeActionSerializable.OpenFile) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val mimeType = context.contentResolver.getType(it)
                val relativePath = getRelativeFilePath(context, it)
                val action = SwipeActionSerializable.OpenFile(
                    filePath = relativePath,
                    mimeType = mimeType
                )
                onFileSelected(action)
            } ?: onDismiss()
        }
    )
    LaunchedEffect(Unit) { launcher.launch("*/*") }
}

fun getRelativeFilePath(context: Context, uri: Uri): String {
    return when {
        DocumentsContract.isDocumentUri(context, uri) -> {
            val docId = DocumentsContract.getDocumentId(uri)
            val splitId = docId.split(":").toTypedArray()
            if (splitId.size > 1 && splitId[0] == "primary") {
                splitId[1].replace("%20", " ").trim('/')
            } else docId
        }
        else -> uri.lastPathSegment ?: "Unknown file"
    }
}
