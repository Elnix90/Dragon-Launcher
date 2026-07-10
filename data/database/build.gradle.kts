plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.elnix.dragonlauncher.database"

}

dependencies {

    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    api(libs.androidx.roomruntime)
    ksp(libs.androidx.roomcompiler)
    api(libs.androidx.room)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:i18n"))
    implementation(project(":core:ktx"))
    implementation(project(":core:settings"))
    implementation(project(":core:base"))

}