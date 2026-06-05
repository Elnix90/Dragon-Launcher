package org.elnix.dragonlauncher.base.cache

import android.graphics.Bitmap
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.Action
import kotlin.reflect.KClass

object ActionIconCache : DragonCache<KClass<out Action>, Bitmap>(Action.actionsNumber)
