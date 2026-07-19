package org.elnix.dragonlauncher.ui.dragon.dialogs

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp


@Composable
public fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    imePadding: Boolean = true,
    scroll: Boolean = true,
    alignment: Alignment = Alignment.BottomCenter,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null
) {

    @SuppressLint("ConfigurationScreenWidthHeight")
    val maxDialogHeight = LocalConfiguration.current.screenHeightDp.dp * 0.9f


    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurface
    ) {
        FullScreenOverlay(
            onDismissRequest = onDismissRequest,
            imePadding = imePadding,
            alignment = alignment
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDialogHeight)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(top = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    icon?.invoke()
                    title?.invoke()
                }

                Box(
                    Modifier
                        .padding(horizontal = 15.dp)
                        .weight(1f, fill = false)
                        .then(
                            if (scroll) {
                                Modifier.verticalScroll(rememberScrollState())
                            } else Modifier
                        )
                ) {
                    text?.invoke()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    dismissButton?.invoke()
                    confirmButton?.invoke()
                }
            }
        }
    }
}
