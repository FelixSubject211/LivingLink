package felix.projekt.livinglink.gatling

import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.time.Duration

class GroupPollSimulation : Simulation() {
    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .header("Content-Type", "application/json")

    private fun randomUsername() = "user${System.currentTimeMillis()}_${Math.random()}"

    private val fullScenario = scenario("Bootstrap and Poll")
        .exec { it.set("username", randomUsername()) }
        .exec(
            http("Register User")
                .post("/auth/register")
                .body(StringBody { session ->
                    """{"username": "${session.getString("username")}", "password": "testpass"}"""
                })
                .check(status().`is`(200))
                .check(jsonPath("$.tokenResponse.accessToken").saveAs("accessToken"))
        )
        .repeat(60).on(
            exec(
                http("Poll Groups")
                    .post("/groups")
                    .header("Authorization") { session -> "Bearer ${session.getString("accessToken")}" }
                    .body(StringBody { session -> """{"currentGroupVersions":{}}""" })
                    .check(status().`is`(200))
            )
        )

    init {
        setUp(
            fullScenario.injectOpen(rampUsers(1000).during(Duration.ofSeconds(10)))
        ).protocols(httpProtocol)
    }
}