import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    namespace = "org.elnix.dragonlauncher.ui.theme"
}

dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.base)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.androidx.lifecycle.process)

    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.ui.graphics)

    implementation(project(":ui:base"))
    implementation(project(":ui:composition"))
    implementation(project(":core:base"))
    implementation(project(":core:enumsui"))
    implementation(project(":core:settings"))
    implementation(project(":core:ktx"))

    api(project(":core:models"))
}
