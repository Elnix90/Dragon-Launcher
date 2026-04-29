package org.elnix.dragonlauncher.ui.base.modifiers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// https://github.com/sosauce/Chocola/blob/866477b3c526e7e9ee551d6f989a4a5f3125f6ae/app/src/main/java/com/sosauce/chocola/utils/Extensions.kt#L92

fun Modifier.selfAlignHorizontally(align: Alignment.Horizontal = Alignment.CenterHorizontally): Modifier {
    return then(
        Modifier
            .fillMaxWidth()
            .wrapContentWidth(align)
    )
}
