plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.elnix.dragonlauncher.enumsui"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    api(libs.androidx.compose.runtime)

    implementation(project(":core:common"))
}
