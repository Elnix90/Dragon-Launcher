
package org.elnix.dragonlauncher.base.cache

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.IconShape

object DrawPathCache : DragonCache<Pair<IconShape, Size>, Path>(200)
