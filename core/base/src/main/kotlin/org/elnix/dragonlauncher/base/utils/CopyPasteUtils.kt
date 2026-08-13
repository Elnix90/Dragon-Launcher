package org.elnix.dragonlauncher.base.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import io.github.elnix90.logging.CONTEXT_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logW
import java.io.File

public object CopyPasteUtils {

    /**
     * Shares content via Intent.ACTION_SEND. Handles both file-based and text-based sharing
     * with automatic fallback if FileProvider is not configured.
     *
     * @param uri The file URI to share (optional). If null, falls back to text sharing.
     * @param text The text content to share as fallback or direct share.
     * @param subject The subject line for the share intent.
     * @param mimeType The MIME type (default: "text/plain").
     * @param chooserTitle The title shown in the share chooser.
     */
    public fun Context.shareContent(
        uri: Uri? = null,
        text: String? = null,
        subject: String = "Share",
        mimeType: String = "text/plain",
        chooserTitle: String = "Share"
    ) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType

                if (uri != null) {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else if (text != null) {
                    putExtra(Intent.EXTRA_TEXT, text)
                }

                putExtra(Intent.EXTRA_SUBJECT, subject)
            }

            startActivity(Intent.createChooser(shareIntent, chooserTitle))

        } catch (e: Exception) {
            logE(CONTEXT_TAG, e) { "Failed to share content" }
            Toast.makeText(this, "Failed to share", Toast.LENGTH_SHORT).show()
        }
    }



    /**
     * Creates a shareable file from the given text, using the app's cache directory.
     *
     * @param text The text content to save.
     * @param filename The name of the file to create.
     * @return A pair of (File in cache, FileProvider Uri) or null if creation failed.
     */
    public fun Context.createShareableTextFile(
        text: String,
        filename: String = "share_${System.currentTimeMillis()}.txt"
    ): Pair<File, Uri>? {
        return try {
            val shareFile = File(cacheDir, filename)
            shareFile.writeText(text)

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                shareFile
            )

            Pair(shareFile, uri)
        } catch (e: Exception) {
            logE(CONTEXT_TAG, e) { "Failed to create shareable text file" }
            null
        }
    }

    /**
     * Creates a shareable file from the given file, using the app's cache directory.
     *
     * @param sourceFile The file to copy to cache.
     * @return A pair of (File in cache, FileProvider Uri) or null if creation failed.
     */
    public fun Context.createShareableFile(sourceFile: File): Pair<File, Uri>? {
        return try {
            val cacheDir = cacheDir
            val shareFile = File(cacheDir, sourceFile.name)
            sourceFile.copyTo(shareFile, overwrite = true)

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                shareFile
            )

            Pair(shareFile, uri)
        } catch (e: Exception) {
            logE(CONTEXT_TAG, e) { "Failed to create shareable file" }
            null
        }
    }

    public fun Context.copyToClipboard(text: String, maxSize: Int = 500 * 1024) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (text.length > maxSize) {
            logW(CONTEXT_TAG) { "Text too large for clipboard (${text.length} bytes), creating file to share" }

            val (_, uri) = createShareableTextFile(text, "clipboard_${System.currentTimeMillis()}.txt")
                ?: run {
                    showToast("Failed to save text to file")
                    return
                }

            shareContent(
                uri = uri,
                subject = "Clipboard Content",
                chooserTitle = "Share Text"
            )
            showToast("Text too large, opening share intent")
            return
        }

        try {
            val clipData = ClipData.newPlainText(getString(R.string.app_name), text)
            clipboardManager.setPrimaryClip(clipData)
            logD(CONTEXT_TAG) { "Copied to clipboard" }
            showToast("Copied to clipboard!")
        } catch (e: Exception) {
            logE(CONTEXT_TAG, e) { "Failed to copy to clipboard" }
            showToast("Failed to copy to clipboard")
        }
    }


    public fun Context.pasteClipboard(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).coerceToText(this)?.toString()
    }
}