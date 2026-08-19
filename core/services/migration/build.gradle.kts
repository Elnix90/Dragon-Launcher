plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.migration"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.timber)

    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.javax.inject)
    api(libs.hilt.android)

    runtimeOnly(libs.kotlinx.coroutines.android)
    runtimeOnly(project(":data:notifications"))
    api(project(":core:i18n"))
    implementation(project(":core:settings"))

    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.kotlinx.serialization.core)
    testImplementation(libs.androidx.compose.ui.unit)
    testImplementation(project(":core:base"))
}
