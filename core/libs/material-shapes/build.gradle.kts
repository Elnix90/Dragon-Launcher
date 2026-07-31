plugins {
    alias(libs.plugins.dragon.library)
}

android {
    namespace = "org.elnix.dragonlauncher.libs.material.shapes"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)

    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
}