import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    namespace = "org.elnix.dragonlauncher.ui.dragon"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.material3)

    runtimeOnly(libs.androidx.lifecycle.process)

    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.ui.text)
    api(libs.androidx.compose.runtime)

    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.settings.core)
    implementation(libs.settings.runtime)

    implementation(project(":ui:base"))
    implementation(project(":ui:theme"))
    implementation(project(":core:base"))
    implementation(project(":ui:composition"))

    api(project(":core:enumsui"))
    api(project(":core:settings"))

    // Compose UI instrumented tests (src/androidTest/)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.bundles.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    debugImplementation(libs.compose.ui.test.manifest)
}
