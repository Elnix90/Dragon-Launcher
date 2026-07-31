import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
    alias(libs.plugins.dragon.serialization)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    namespace = "org.elnix.dragonlauncher.ui.composition"
}

dependencies {
    api(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.text)

    api(project(":core:common"))
    api(project(":core:models"))
}
