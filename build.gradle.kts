// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka)

    id("com.autonomousapps.dependency-analysis") version "3.14.1"
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
    apply(plugin = "org.jetbrains.dokka")
}