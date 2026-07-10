plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.elnix.dragonlauncher.ui.dragon"

    buildFeatures {
        compose = true
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

    implementation(project(":core:ui:base"))
    implementation(project(":core:ui:theme"))

    implementation(project(":core:base"))
    implementation(project(":core:common"))
    api(project(":core:enumsui"))
    api(project(":core:settings"))
}
