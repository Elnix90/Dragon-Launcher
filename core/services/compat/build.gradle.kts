plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.compat"
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.datastore.core)
    implementation(libs.dragon.logging)
    implementation(libs.androidx.compose.ui.graphics)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:base"))
    implementation(project(":core:i18n"))
}
