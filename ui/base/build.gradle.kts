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
    namespace = "org.elnix.dragonlauncher.ui.base"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.runtime.retain)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.settings.runtime)


    runtimeOnly(libs.androidx.lifecycle.process)

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.graphics.shapes)
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.animation)

    api(project(":core:enumsui"))
    api(project(":core:models"))
    api(project(":core:common"))
    api(project(":core:settings"))
    api(project(":core:base"))
}
