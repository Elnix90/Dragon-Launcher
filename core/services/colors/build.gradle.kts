plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.colors"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.compose.material3)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:ktx"))
    implementation(project(":data:applications"))
    implementation(project(":data:notifications"))
    implementation(project(":core:settings"))
    implementation(project(":core:profiles"))
    implementation(project(":core:base"))
}
