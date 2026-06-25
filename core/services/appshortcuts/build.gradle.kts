import com.android.build.api.dsl.LibraryExtension


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)

}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.services.appshortcuts"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
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