import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.base"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(platform(libs.androidx.compose.bom))

    api(libs.androidx.ui.graphics)

}
