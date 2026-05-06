package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import org.elnix.dragonlauncher.common.messyfolder.isNotBlankJson

@Composable
inline fun <reified T> rememberDecodedObject(
    jsonString: String,
    default: T,
    json: kotlinx.serialization.json.Json,
    crossinline onError: (Exception) -> Unit = {}
): T {
    return remember(jsonString) {
        derivedStateOf {
            if (jsonString.isNotBlankJson) {
                try {
                    json.decodeFromString<T>(jsonString)
                } catch (e: Exception) {
                    onError(e)
                    default
                }
            } else {
                default
            }
        }
    }.value
}
