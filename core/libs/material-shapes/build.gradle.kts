plugins {
    alias(libs.plugins.dragon.library)
}

android {
    namespace = "org.elnix.dragonlauncher.libs.material.shapes"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.util)

    api(libs.androidx.graphics.shapes)
    api(libs.androidx.ui.graphics)

    runtimeOnly(libs.kotlinx.coroutines.android)
}
