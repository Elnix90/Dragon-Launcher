package org.elnix.dragonlauncher.base.cache

import androidx.compose.ui.graphics.Path
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape

object NestIntersectionShapesPathCache : DragonCache<IntersectionShape, Path>(200)
