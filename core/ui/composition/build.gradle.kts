import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    namespace = "org.elnix.dragonlauncher.ui.composition"

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.text)

    api(project(":core:common"))
    api(project(":core:models"))
}
