plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.elnix.dragonlauncher.services.system"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.dragon.logging)
    implementation(libs.timber)

    api(libs.kotlinx.coroutines.core)

    api(project(":core:common"))
    implementation(project(":core:settings"))
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
}
