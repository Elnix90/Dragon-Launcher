package org.elnix.dragonlauncher.ui.helpers

import android.content.Intent
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
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val mimeType = context.contentResolver.getType(it)

                // Take persistable permission
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val action = SwipeActionSerializable.OpenFile(
                    uri = it.toString(),
                    mimeType = mimeType
                )
                onFileSelected(action)
            } ?: onDismiss()
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf("*/*"))
    }

}
