package dragonlauncher.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DragonAndroidSerializationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
    }
}
