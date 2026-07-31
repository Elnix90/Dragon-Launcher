package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun ContributorItem(
    name: String,
    @DrawableRes imageRes: Int,
    description: String? = null,
    githubUrl: String
) {
    val ctx = LocalContext.current
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = { ctx.copyToClipboard(githubUrl) },
                onClick = { uriHandler.openUri(githubUrl) }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "$name profile picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )

        TextWithDescription(
            text = name,
            description = description,
            modifier = Modifier
                .weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.open_in_new),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
