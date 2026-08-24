package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import org.elnix.dragonlauncher.base.model.models.SocialLink
import org.elnix.dragonlauncher.ui.base.components.BoxedIcon
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DragonGroupScope.ContributorItem(
    name: String,
    shape: RoundedPolygon,
    @DrawableRes imageRes: Int,
    description: String? = null,
    vararg socialLinks: SocialLink
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier.dragonSettingGroup(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "$name profile picture",
            modifier = Modifier
                .size(48.dp)
                .clip(shape.toShape()),
            contentScale = ContentScale.Fit
        )

        TextWithDescription(
            text = name,
            description = description,
            modifier = Modifier
                .weight(1f)
        )

        socialLinks.forEach {
            BoxedIcon(it.icon) {
                uriHandler.openUri(it.url)
            }
        }
    }
}
