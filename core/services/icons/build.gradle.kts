plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.elnix.dragonlauncher.core.services.icons"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.palette)
    implementation(libs.bundles.androidx.lifecycle)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.dragon.logging)

    implementation(project(":core:common"))
    implementation(project(":core:settings"))
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(project(":core:base"))
    implementation(project(":core:ktx"))
    implementation(project(":data:applications"))
    implementation(project(":data:database"))
    implementation(project(":data:appoverrides"))
    implementation(project(":core:services:points"))
    implementation(project(":core:services:colors"))
    implementation(project(":core:services:compat"))
    implementation(project(":core:services:appshortcuts"))
}