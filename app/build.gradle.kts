import com.android.build.api.dsl.ApplicationExtension
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.dragon.application)
}


val dotenv = Properties().apply {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

fun env(name: String): String? =
    System.getenv(name) ?: dotenv.getProperty(name)


extensions.configure<ApplicationExtension> {
    namespace = "org.elnix.dragonlauncher"

    defaultConfig {
        applicationId = "org.elnix.dragonlauncher"
        versionName = "4.0.0"
        versionCode = 57
    }

    val hasSigningConfig = env("KEYSTORE_FILE") != null &&
            env("KEYSTORE_PASSWORD") != null &&
            env("KEY_ALIAS") != null &&
            env("KEY_PASSWORD") != null

    signingConfigs {
        create("release") {
            if (hasSigningConfig) {
                storeFile = file(env("KEYSTORE_FILE")!!)
                storePassword = env("KEYSTORE_PASSWORD")!!
                keyAlias = env("KEY_ALIAS")!!
                keyPassword = env("KEY_PASSWORD")!!
            } else {
                println("No signing config found, APK will be unsigned")
            }
        }
    }

    buildTypes {
        if (hasSigningConfig) {
            // I use signing config because when I try to install the debug app, and that the release is installed, it won't work.
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
            }

            getByName("beta") {
                signingConfig = signingConfigs.getByName("release")
            }

            getByName("debug") {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.dagger)
    implementation(libs.hilt.core)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(libs.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.timber)

    // DO NOT FUCKING DELTE THIS!!!!!
    implementation(libs.shizuku.provider)


    ksp(libs.hilt.compiler)

    implementation(project(":core:ktx"))
    implementation(project(":core:base"))
    implementation(project(":core:i18n"))
    implementation(project(":core:models"))
    implementation(project(":core:profiles"))
    implementation(project(":core:settings"))
    implementation(project(":core:permissions"))
    implementation(project(":core:services:fonts"))
    implementation(project(":core:services:icons"))
    implementation(project(":core:services:timer"))
    implementation(project(":core:services:badges"))
    implementation(project(":core:services:colors"))
    implementation(project(":core:services:compat"))
    implementation(project(":core:services:points"))
    implementation(project(":core:services:recents"))
    implementation(project(":core:services:system"))
    implementation(project(":core:services:security"))
    implementation(project(":core:services:migration"))
    implementation(project(":core:services:appshortcuts"))

    implementation(project(":data:database"))
    implementation(project(":data:workspaces"))
    implementation(project(":data:applications"))
    implementation(project(":data:appoverrides"))
    implementation(project(":data:notifications"))

    implementation(project(":ui:base"))
    implementation(project(":ui:main"))
    implementation(project(":ui:theme"))
}


// Copy files in the fastlane/metadata dir to the assets folder, where they are compiled and added to the app
tasks.register<Copy>("copyChangelogsToAssets") {
    description = "Copy the fastlane changelogs folder directly to the assets folder to have them in-app"
    from("../fastlane/metadata/android/en-US/changelogs")
    into(file("src/main/assets/changelogs"))
    include("*.txt")
}

// Download the extensions registry from GitHub
tasks.register("downloadExtensionsRegistry") {
    description = "Downloads the extensions registry JSON from GitHub"
    outputs.upToDateWhen { false } // Ignore cache for this task
    val registryUrl = "https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/main/extensions-registry.json"
    val outputFile = file("src/main/assets/extensions-registry.json")

    inputs.property("url", registryUrl)
    outputs.file(outputFile)

    doLast {
        println("Downloading extensions registry from $registryUrl...")
        outputFile.parentFile.mkdirs()
        try {
            URI(registryUrl).toURL().openStream().use { input: InputStream ->
                outputFile.outputStream().use { output: OutputStream ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            println("WARNING: Failed to download extensions registry: ${e.message}")
            // Create an empty registry if download fails to avoid runtime errors
            if (!outputFile.exists()) {
                outputFile.writeText("{\"extensions\": []}")
            }
        }
    }
}

// Use preBuild tasks instead of merge* (they exist in AGP)
if (!gradle.startParameter.taskRequests.any { it.args.contains("buildHealth") }) {
    tasks.named("preBuild") {
        dependsOn("copyChangelogsToAssets")

//        // Only download extensions registry on release builds
//        val isReleaseVariant = gradle.startParameter.taskRequests.any {
//            it.args.any { arg -> arg.contains("Release", ignoreCase = true) }
//        }
//        if (isReleaseVariant) {
        dependsOn("downloadExtensionsRegistry")
//        }
    }
} else {
    println("Gradle in using build health, not running preBuild")
}
