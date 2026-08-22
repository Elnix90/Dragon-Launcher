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
    api(libs.androidx.compose.ui.text)

    runtimeOnly(libs.androidx.roomruntime)
    runtimeOnly(project(":data:notifications"))

    api(project(":core:base"))
    api(project(":core:enumsui"))
}
