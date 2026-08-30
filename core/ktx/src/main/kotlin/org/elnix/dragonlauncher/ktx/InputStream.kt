package org.elnix.dragonlauncher.ktx

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream

public fun InputStream.asBitmap(options: BitmapFactory.Options? = null): Bitmap? =
    BitmapFactory.decodeStream(
        this,
        null,
        options
    )
