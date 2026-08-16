plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.settings) // My plugin 🤎
}

android {
    namespace = "org.elnix.dragonlauncher.settings"
}

dependencies {
    implementation(libs.androidx.datastore.core)
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.unit)

    api(libs.androidx.datastore.preferences.core)
    api(libs.androidx.ui.graphics)
    api(libs.settings.core)

    // My plugin 🤎
    implementation(libs.settings.annotations)

    implementation(libs.dragon.logging)

    implementation(project(":core:ktx"))
    api(project(":core:base"))
    implementation(project(":core:i18n"))
    api(project(":core:enumsui"))
}
