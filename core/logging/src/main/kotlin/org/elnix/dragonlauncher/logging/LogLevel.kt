package org.elnix.dragonlauncher.logging

import android.util.Log
import androidx.compose.ui.graphics.Color

val Int.logLevelName: String
    get() = when (this.coerceIn(2..7)) {
        Log.VERBOSE -> "Verbose"
        Log.DEBUG -> "Debug"
        Log.INFO -> "Info"
        Log.WARN -> "Warning"
        Log.ERROR -> "Error"
        else -> "No Logging"
    }

val Int.logLevelChar: Char
    get() = when (this.coerceIn(2..7)) {
        Log.VERBOSE -> 'V'
        Log.DEBUG -> 'D'
        Log.INFO -> 'I'
        Log.WARN -> 'W'
        Log.ERROR -> 'E'
        else -> '?'
    }



val Char.logLevel: Int?
    get() = when (this.uppercaseChar()) {
        'V' -> Log.VERBOSE
        'D' -> Log.DEBUG
        'I' -> Log.INFO
        'W' -> Log.WARN
        'E' -> Log.ERROR
        else -> null
    }


val Int.logLevelColor: Color?
    get() = when (this.coerceIn(2..7)) {
        Log.VERBOSE -> Color.LightGray
        Log.DEBUG -> Color.Cyan
        Log.INFO -> Color.Green
        Log.WARN -> Color.Yellow
        Log.ERROR -> Color.Red
        else -> null
    }