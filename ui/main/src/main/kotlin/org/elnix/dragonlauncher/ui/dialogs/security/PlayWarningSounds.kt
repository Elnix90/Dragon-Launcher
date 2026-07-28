package org.elnix.dragonlauncher.ui.dialogs.security

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.i18n.R

@Composable
fun PlayWarningSounds(
    failedTries: Int,
    superWarningMode: Boolean,
    superWarningModeSound: Int,
    alarmSoundEnabled: Boolean,
    metalPipesSoundEnabled: Boolean
) {
    val ctx = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    var alarmLoaded by remember { mutableStateOf(false) }
    var metalLoaded by remember { mutableStateOf(false) }

    val alarmSoundId = remember {
        soundPool.load(ctx, R.raw.warning, 1)
    }

    val metalSoundId = remember {
        soundPool.load(ctx, R.raw.metal_pipe, 1)
    }

    DisposableEffect(Unit) {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                if (sampleId == alarmSoundId) alarmLoaded = true
                if (sampleId == metalSoundId) metalLoaded = true
            }
        }

        onDispose { soundPool.release() }
    }

    LaunchedEffect(failedTries, superWarningMode) {
        if (failedTries > 0 && superWarningMode && superWarningModeSound > 0) {

            val audioManager =
                ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val maxVolume =
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                superWarningModeSound.coerceIn(0, maxVolume),
                0
            )

            if (alarmSoundEnabled && alarmLoaded) {
                soundPool.play(alarmSoundId, 1f, 1f, 1, -1, 1f)
            }

            if (metalPipesSoundEnabled && metalLoaded) {
                soundPool.play(metalSoundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }
}