package org.elnix.dragonlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.common.utils.Constants.Logging.FONT_RECEIVER_TAG
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FontReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val action = intent?.action ?: "<null>"
            logD(FONT_RECEIVER_TAG) { "Received intent action=$action" }
            if (action == "org.elnix.dragonlauncher.ACTION_FONTS_RESULT") {
                val fontPath = intent?.getStringExtra("FONT_PATH")
                val fontName = intent?.getStringExtra("FONT_NAME") ?: "unknown"
                logD(FONT_RECEIVER_TAG) { "ACTION_FONTS_RESULT for $fontName -> path=$fontPath" }

                if (fontPath != null && context != null) {
                    try {
                        // If it's a content URI, we must copy via content resolver
                        if (fontPath.startsWith("content://")) {
                            val uri = fontPath.toUri()
                            val destDir = File(context.getExternalFilesDir(null), "fonts")
                            if (!destDir.exists()) destDir.mkdirs()
                            val dest = File(destDir, "$fontName.ttf")

                            context.contentResolver.openInputStream(uri)?.use { input ->
                                FileOutputStream(dest).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            logD(FONT_RECEIVER_TAG) { "Copied font from URI to ${dest.absolutePath}" }
                        } else {
                            // Fallback to direct file copy if it's a raw path
                            val src = File(fontPath)
                            val destDir = File(context.getExternalFilesDir(null), "fonts")
                            if (!destDir.exists()) destDir.mkdirs()
                            val dest = File(destDir, src.name)

                            FileInputStream(src).use { input ->
                                FileOutputStream(dest).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            logD(FONT_RECEIVER_TAG) { "Copied font file to ${dest.absolutePath}" }
                        }
                    } catch (e: Exception) {
                        logE(FONT_RECEIVER_TAG, e) { "Failed to copy font" }
                    }
                }
            }
        } catch (e: Exception) {
            logE(FONT_RECEIVER_TAG, e) { "Receiver error" }
        }
    }
}