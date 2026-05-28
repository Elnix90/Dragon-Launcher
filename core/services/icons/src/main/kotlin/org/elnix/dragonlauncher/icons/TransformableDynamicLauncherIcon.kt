package org.elnix.dragonlauncher.icons

import org.elnix.dragonlauncher.icons.transformations.LauncherIconTransformation

internal interface TransformableDynamicLauncherIcon {
    fun setTransformations(transformations: List<LauncherIconTransformation>)
}