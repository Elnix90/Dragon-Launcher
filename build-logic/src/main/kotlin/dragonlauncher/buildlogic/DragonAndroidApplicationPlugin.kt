package dragonlauncher.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class DragonAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("com.autonomousapps.dependency-analysis")

            configureKotlinAndroid(
                enableExplicitApi = false,
                enablePropertyParamAnnotationFlag = false,
            )
            forceKotlinMetadataResolution()


            extensions.configure(ApplicationExtension::class.java) {
                compileSdk {
                    version = release(COMPILE_SDK)
                }

                defaultConfig {
                    minSdk = MIN_SDK
                    targetSdk = TARGET_SDK
                }

                lint {
                    checkReleaseBuilds = false
                    abortOnError = false
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        versionNameSuffix = " ($VERSION_CODE)"
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }

                    create("beta") {
                        isMinifyEnabled = true
                        applicationIdSuffix = ".beta"
                        versionNameSuffix = " ($VERSION_CODE)-beta"
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }

                    debug {
                        isDebuggable = true
                        isMinifyEnabled = false
                        applicationIdSuffix = ".debug"
                        versionNameSuffix = " ($VERSION_CODE)-debug"
                    }
                }

                compileOptions {
                    sourceCompatibility = JAVA_VERSION
                    targetCompatibility = JAVA_VERSION
                }

                buildFeatures {
                    compose = true
                    buildConfig = true
                    resValues = true
                }

                packaging {
                    jniLibs.keepDebugSymbols.add("**/*.so")
                }

                dependenciesInfo {
                    includeInApk = false
                    includeInBundle = false
                }
            }
        }
    }
}
