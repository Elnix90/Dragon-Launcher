package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.elnix90.logging.ANGLE_LINE_TAG
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultEndCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultHoldCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultLineCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultStartCustomObject
import org.elnix.dragonlauncher.ktx.isNotBlankJson
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore

@Composable
private inline fun <reified T> rememberDecodedObject(
    jsonString: String,
    default: T,
    json: Json,
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


object CustomObjectJson : DragonJson<CustomObject>() {
        data class AngleLineObjects(
            val line: CustomObject,
            val angleLine: CustomObject,
            val startLine: CustomObject,
            val endLine: CustomObject
    )

    @Composable
    fun rememberAngleLineObjects(): AngleLineObjects {
        val lineJson by AngleLineSettingsStore.lineJson.asState()
        val angleLineJson by AngleLineSettingsStore.angleLineJson.asState()
        val startLineJson by AngleLineSettingsStore.startLineJson.asState()
        val endLineJson by AngleLineSettingsStore.endLineJson.asState()

        val lineObject = rememberDecodedObject(
            jsonString = lineJson,
            default = defaultLineCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding lineObject" } }

        val angleLineObject = rememberDecodedObject(
            jsonString = angleLineJson,
            default = defaultAngleCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding angleLineObject" } }

        val startLineObject = rememberDecodedObject(
            jsonString = startLineJson,
            default = defaultStartCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding startLineObject" } }

        val endLineObject = rememberDecodedObject(
            jsonString = endLineJson,
            default = defaultEndCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding endLineObject" } }

        return AngleLineObjects(
            line = lineObject,
            angleLine = angleLineObject,
            startLine = startLineObject,
            endLine = endLineObject
        )
    }



    @Composable
    fun rememberHoldCustomObject(): CustomObject {
        val holdCustomObjectJson by HoldToActivateArcSettingsStore.holdToActivateArcCustomObject.asState()
        return rememberDecodedObject(
            jsonString = holdCustomObjectJson,
            default = defaultHoldCustomObject,
            json = json
        ) {
            logE(ANGLE_LINE_TAG, it) { "Error decoding endLineObject" }
        }
    }
}