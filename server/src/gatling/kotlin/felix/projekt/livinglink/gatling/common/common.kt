package felix.projekt.livinglink.gatling.common

import io.gatling.javaapi.http.HttpDsl.http

val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .header("Content-Type", "application/json")

fun authHeader(token: String) = "Bearer $token"