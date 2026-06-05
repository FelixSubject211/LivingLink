plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.mokkery)
    application
}

group = "com.felix"
version = "1.0.0-SNAPSHOT"

application {
    mainClass.set("com.felix.livinglink.server.MainKt")
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set("1.6.0")
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.mcp.kotlin.sdk.server)
    implementation(libs.logback.classic)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(libs.ktor.server.cio)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.cors)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.konsist)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
