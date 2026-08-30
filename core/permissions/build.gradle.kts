plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.permissions"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.appcompat)
    implementation(libs.dragon.logging)
    implementation(libs.core)
    implementation(libs.javax.inject)
    implementation(libs.timber)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)

    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(project(":core:ktx"))
}
