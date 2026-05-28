import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.workspaces"

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
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.commons.text)

    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    implementation(project(":core:base"))
    implementation(project(":core:settings"))
    implementation(project(":core:ktx"))
    implementation(project(":core:common"))
    implementation(project(":core:profiles"))
    implementation(project(":core:services:compat"))
}