plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "org.elnix.dragonlauncher.ui.composition"

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(libs.androidx.compose.runtime)

    api(project(":core:common"))
    api(project(":core:models"))
}
