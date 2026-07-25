package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.MonospaceScrollableText
import org.json.JSONObject

@Composable
fun ThemeJsonPopup(
    json: JSONObject,
    onDismiss: () -> Unit
) {
    val themeString: String = json.toString(2)

    CustomAlertDialog(
        modifier = Modifier.padding(15.dp),
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        scroll = false,
        text = {
            MonospaceScrollableText(themeString.lines())
        }
    )
}
