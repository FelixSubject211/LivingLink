plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "com.felix"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "com.felix.LivingLinkMcpMainKt"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.mcp.kotlin.sdk.server)
    implementation(libs.logback.classic)
    testImplementation(libs.kotlin.test)
}