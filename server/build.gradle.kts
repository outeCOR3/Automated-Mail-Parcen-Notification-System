plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "org.example.project"
version = "1.0.0"
application {
    mainClass.set("org.example.project.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(libs.jbcrypt)
    implementation(libs.kotlinx.serialization.json.v163)
    implementation(libs.exposed.java.time.v0440)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)
    implementation(projects.shared)
    implementation(projects.composeApp)
    implementation(libs.logback)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.tests)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json.v238)
    implementation(libs.hikari)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.core.v230)  // Ktor core
    implementation(libs.ktor.client.cio.v230)   // Ktor CIO engine
    implementation(libs.ktor.client.json)   // Ktor JSON serialization


    testImplementation(libs.kotlin.test.junit)
}