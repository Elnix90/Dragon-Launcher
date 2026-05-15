package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.common.messyfolder.isNotBlankJson
import org.elnix.dragonlauncher.common.serializables.ColorSerializer
import org.elnix.dragonlauncher.common.serializables.CustomObjectSerializable
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.asState

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


object CustomObjectJson {

    private val json = Json {
        serializersModule = SerializersModule {
            contextual(Color::class, ColorSerializer)
        }
    }

    fun encode(customObject: CustomObjectSerializable): String =
        json.encodeToString(CustomObjectSerializable.serializer(), customObject)



    data class AngleLineObjects(
        val line: CustomObjectSerializable,
        val angleLine: CustomObjectSerializable,
        val startLine: CustomObjectSerializable,
        val endLine: CustomObjectSerializable
    )

    @Composable
    fun rememberAngleLineObjects(): AngleLineObjects {
        val lineJson by AngleLineSettingsStore.lineJson.asState()
        val angleLineJson by AngleLineSettingsStore.angleLineJson.asState()
        val startLineJson by AngleLineSettingsStore.startLineJson.asState()
        val endLineJson by AngleLineSettingsStore.endLineJson.asState()

        val lineObject = rememberDecodedObject(
            jsonString = lineJson,
            default = UiConstants.defaultLineCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding lineObject" } }

        val angleLineObject = rememberDecodedObject(
            jsonString = angleLineJson,
            default = UiConstants.defaultAngleCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding angleLineObject" } }

        val startLineObject = rememberDecodedObject(
            jsonString = startLineJson,
            default = UiConstants.defaultStartCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding startLineObject" } }

        val endLineObject = rememberDecodedObject(
            jsonString = endLineJson,
            default = UiConstants.defaultEndCustomObject,
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
    fun rememberHoldCustomObject(): CustomObjectSerializable {
        val holdCustomObjectJson by HoldToActivateArcSettingsStore.holdToActivateArcCustomObject.asState()
        return rememberDecodedObject(
            jsonString = holdCustomObjectJson,
            default = UiConstants.defaultHoldCustomObject,
            json = json
        ) {
            logE(ANGLE_LINE_TAG, it) { "Error decoding endLineObject" }
        }
    }
}