import org.gradle.kotlin.dsl.`java-gradle-plugin`
import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "org.elnix.dragonlauncher.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.kotlin.gradle.plugin.api)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("dragonLibrary") {
            id = "dragon.library"
            implementationClass = "dragonlauncher.buildlogic.DragonAndroidLibraryPlugin"
        }
        register("dragonCompose") {
            id = "dragon.compose"
            implementationClass = "dragonlauncher.buildlogic.DragonAndroidComposePlugin"
        }
        register("dragonSerialization") {
            id = "dragon.serialization"
            implementationClass = "dragonlauncher.buildlogic.DragonAndroidSerializationPlugin"
        }
        register("dragonHilt") {
            id = "dragon.hilt"
            implementationClass = "dragonlauncher.buildlogic.DragonHiltPlugin"
        }
        register("dragonApplication") {
            id = "dragon.application"
            implementationClass = "dragonlauncher.buildlogic.DragonAndroidApplicationPlugin"
        }
    }
}

