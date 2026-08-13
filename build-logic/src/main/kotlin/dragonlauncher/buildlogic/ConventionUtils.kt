package dragonlauncher.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 37

/**
 * Version code, will be used in the Gradle build to add it to the version name, but only at build time to avoid fdroid failures
 */
internal const val VERSION_CODE = "Caterpillar"
internal val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_21

internal const val KOTLIN_METADATA_VERSION = "2.3.0-Beta1"

internal fun Project.configureKotlinAndroid(
    enableExplicitApi: Boolean = true,
    enablePropertyParamAnnotationFlag: Boolean = true,
) {
    extensions.configure(KotlinAndroidProjectExtension::class.java) {
        jvmToolchain(JAVA_VERSION.majorVersion.toInt())
        if (enableExplicitApi) {
            explicitApi()
        }
        if (enablePropertyParamAnnotationFlag) {
            compilerOptions {
                freeCompilerArgs.value(listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode"))
            }
        }
    }
}

internal fun Project.forceKotlinMetadataResolution() {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-metadata-jvm:$KOTLIN_METADATA_VERSION")
        }
    }
}
