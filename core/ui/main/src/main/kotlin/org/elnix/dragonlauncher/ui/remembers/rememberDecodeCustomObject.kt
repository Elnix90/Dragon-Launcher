package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import org.elnix.dragonlauncher.settings.stores.objects.AngleObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.EndObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.HoldToActivateObject
import org.elnix.dragonlauncher.settings.stores.objects.LineObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.StartObjectSettingStore

@Composable
private inline fun <reified T> rememberDecodedObject(
    jsonString: String,
    default: T,
    json: Json,
    crossinline onError: (Exception) -> Unit = {}
): State<T> {
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
    }
}


public object CustomObjectJson : DragonJson<CustomObject>() {
    public data class AngleLineObjects(
        val line: CustomObject,
        val angleLine: CustomObject,
        val startLine: CustomObject,
        val endLine: CustomObject
    )

    @Composable
    public fun rememberAngleLineObjects(): AngleLineObjects {
        val lineJson by LineObjectSettingStore.jsonSetting.asState()
        val angleLineJson by AngleObjectSettingStore.jsonSetting.asState()
        val startLineJson by StartObjectSettingStore.jsonSetting.asState()
        val endLineJson by EndObjectSettingStore.jsonSetting.asState()

        val lineObject by rememberDecodedObject(
            jsonString = lineJson,
            default = defaultLineCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding lineObject" } }

        val angleLineObject by rememberDecodedObject(
            jsonString = angleLineJson,
            default = defaultAngleCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding angleLineObject" } }

        val startLineObject by rememberDecodedObject(
            jsonString = startLineJson,
            default = defaultStartCustomObject,
            json = json
        ) { logE(ANGLE_LINE_TAG, it) { "Error decoding startLineObject" } }

        val endLineObject by rememberDecodedObject(
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
    public fun rememberHoldCustomObject(): State<CustomObject> {
        val holdCustomObjectJson by HoldToActivateObject.jsonSetting.asState()
        return rememberDecodedObject(
            jsonString = holdCustomObjectJson,
            default = defaultHoldCustomObject,
            json = json
        ) {
            logE(ANGLE_LINE_TAG, it) { "Error decoding endLineObject" }
        }
    }
}