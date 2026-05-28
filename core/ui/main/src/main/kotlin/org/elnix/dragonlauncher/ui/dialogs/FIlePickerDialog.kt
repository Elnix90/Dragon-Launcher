package org.elnix.dragonlauncher.ui.dialogs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.common.serializables.SwipeAction

@Composable
fun FilePickerDialog(
    onDismiss: () -> Unit,
    onFileSelected: (SwipeAction.OpenFile) -> Unit
) {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val mimeType = ctx.contentResolver.getType(it)

                // Take persistable permission
                ctx.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val action = SwipeAction.OpenFile(
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
