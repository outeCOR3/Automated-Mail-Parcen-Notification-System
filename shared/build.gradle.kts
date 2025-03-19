import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)

}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
            commonMain.dependencies {
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)


                implementation(libs.kotlinx.serialization.json.v151) // This line
                implementation(libs.kotlinx.serialization.json.v161)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.client.core.v230)  // Ktor core
                implementation(libs.ktor.client.cio.v230)   // Ktor CIO engine
                implementation(libs.ktor.client.json)   // Ktor JSON serialization




            }
        val jvmMain by getting {
            dependencies {
                implementation(libs.exposed.core)
                implementation(libs.exposed.dao)
                implementation(libs.exposed.jdbc)
                implementation(libs.exposed.java.time)
            }
        }
    }
}
android {
    namespace = "org.example.project.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
