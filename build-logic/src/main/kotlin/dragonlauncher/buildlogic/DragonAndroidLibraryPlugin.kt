package dragonlauncher.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DragonAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("com.autonomousapps.dependency-analysis")

            configureKotlinAndroid()
            forceKotlinMetadataResolution()

            extensions.configure(LibraryExtension::class.java) {
                compileSdk {
                    version = release(COMPILE_SDK)
                }

                defaultConfig {
                    minSdk = MIN_SDK
                    consumerProguardFiles("consumer-rules.pro")
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                    getByName("debug") {
                        isMinifyEnabled = false
                    }
                    create("beta") {
                        isMinifyEnabled = false
                    }
                }

                compileOptions {
                    sourceCompatibility = JAVA_VERSION
                    targetCompatibility = JAVA_VERSION
                }
            }

            val androidTestDir = file("src/androidTest")
            val hasAndroidTestSources =
                androidTestDir.exists() &&
                    androidTestDir.walkTopDown().any { it.isFile && (it.extension == "kt" || it.extension == "java") }

            if (!hasAndroidTestSources) {
                tasks.configureEach {
                    if (name.startsWith("connected") && name.endsWith("AndroidTest")) {
                        enabled = false
                    }
                }
            }
        }
    }
}
