plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.mokkery)
    application
}

group = "felix.projekt.livinglink"
version = "1.0.0"

application {
    mainClass.set("felix.projekt.livinglink.server.ApplicationKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

val gatlingImplementation = configurations.create("gatlingImplementation") {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(configurations.implementation.get())
}

val gatlingSourceSet = sourceSets.create("gatling") {
    kotlin.srcDir("src/gatling/kotlin")
    resources.srcDir("src/gatling/resources")
    runtimeClasspath += output + compileClasspath
}

tasks.register<JavaExec>("runGatlingSimulation") {
    group = "performance"
    description = "Run a Gatling simulation"

    classpath = gatlingSourceSet.runtimeClasspath
    mainClass.set("io.gatling.app.Gatling")

    val simulation = project.findProperty("gatlingSimulation")?.toString()
        ?: "felix.projekt.livinglink.LoginRegisterSimulation"

    val resultsDir = layout.buildDirectory.dir("gatling-results").get().asFile.absolutePath

    args = listOf(
        "-s", simulation,
        "-rf", resultsDir
    )

    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "--add-opens", "java.base/java.nio=ALL-UNNAMED",
        "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED"
    )
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.dotenv)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.keycloak.admin.client)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.mongodb.driver.core)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.lettuce)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)

    gatlingImplementation(libs.gatling.core.java)
    gatlingImplementation(libs.gatling.http.java)
    gatlingImplementation(libs.gatling.app)
    gatlingImplementation(libs.gatling.recorder)
    gatlingImplementation(libs.gatling.charts.highcharts)
}