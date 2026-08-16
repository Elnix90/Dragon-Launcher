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
    implementation(libs.core)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.settings.runtime)
    implementation(libs.androidx.graphics.shapes)

    runtimeOnly(libs.androidx.lifecycle.process)

    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.ui.text)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.ui.unit)
    api(libs.androidx.material3)
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.settings.core)

    implementation(project(":ui:base"))
    implementation(project(":ui:theme"))
    implementation(project(":core:base"))
    implementation(project(":ui:composition"))
    implementation(project(":core:settings"))
    implementation(project(":core:i18n"))
    implementation(project(":core:ktx"))
    implementation(project(":core:libs:material-shapes"))

    api(project(":core:enumsui"))

    // Compose UI instrumented tests (src/androidTest/)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.activity)
    androidTestRuntimeOnly(libs.androidx.test.runner)

    debugRuntimeOnly(libs.compose.ui.test.manifest)
}
