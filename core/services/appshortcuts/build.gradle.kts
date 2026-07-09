plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)

}

android {
    namespace = "org.elnix.dragonlauncher.services.appshortcuts"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.dragon.logging)

    implementation(libs.commons.text)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":data:applications"))
    implementation(project(":core:permissions"))
    implementation(project(":core:base"))
    implementation(project(":core:ktx"))
    implementation(project(":core:profiles"))
    implementation(project(":core:i18n"))
}