plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.settings) // My plugin 🤎
}

android {
    namespace = "org.elnix.dragonlauncher.settings"
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.datastore.core)
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material3)

    api(libs.androidx.datastore.preferences.core)
    api(libs.kotlinx.coroutines.core)

    // My plugin 🤎
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(libs.settings.annotations)

    implementation(libs.dragon.logging)

    implementation(project(":core:base"))
    api(project(":core:common"))
    api(project(":core:enumsui"))
}
