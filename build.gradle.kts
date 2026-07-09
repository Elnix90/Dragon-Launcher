
import com.android.build.gradle.internal.dsl.LibraryExtensionImpl
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.settings) apply false

    alias(libs.plugins.dependency.analysis)
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
        }
    }
}

subprojects {
    apply(plugin = "com.autonomousapps.dependency-analysis")

    plugins.withId("com.android.library") {
        configure<KotlinAndroidProjectExtension> {
            jvmToolchain(21)
            explicitApi()

            compilerOptions {
                freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
            }
        }

        extensions.configure<LibraryExtensionImpl> {
            compileSdk {
                version = release(libs.versions.compileSdk.get().toInt())
            }

            defaultConfig {
                minSdk = libs.versions.minSdk.get().toInt()
                consumerProguardFiles("consumer-rules.pro")
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
    }
}
