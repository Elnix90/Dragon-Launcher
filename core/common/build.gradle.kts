import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.common"

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
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.biometric)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.timber)
    implementation(libs.stringsimilarity)

    api(libs.gson)
    api(libs.androidx.activity)
    api(libs.androidx.compose.ui.unit)
    api(libs.kotlinx.serialization.core)
    api(libs.androidx.compose.runtime)

    api(project(":core:logging"))
    api(project(":core:base"))
    api(project(":core:i18n"))
    api(project(":core:ktx"))
}
