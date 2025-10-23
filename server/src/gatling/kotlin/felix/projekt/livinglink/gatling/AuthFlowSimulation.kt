package felix.projekt.livinglink.gatling

import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.time.Duration

class AuthFlowSimulation : Simulation() {
    private val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .header("Content-Type", "application/json")

    private fun randomUsername() = "user${System.currentTimeMillis()}_${Math.random()}"

    private val authScenario = scenario("Full Auth Flow")
        .exec { it.set("username", randomUsername()) }
        .exec(
            http("Register User")
                .post("/auth/register")
                .body(StringBody { session ->
                    """{"username": "${session.getString("username")}", "password": "testpass"}"""
                })
                .check(status().`is`(200))
        )
        .exec(
            http("Login User")
                .post("/auth/login")
                .body(StringBody { session ->
                    """{"username": "${session.getString("username")}", "password": "testpass"}"""
                })
                .check(status().`is`(200))
                .check(jsonPath("$.tokenResponse.accessToken").saveAs("accessToken"))
                .check(jsonPath("$.tokenResponse.refreshToken").saveAs("refreshToken"))
        )
        .exec(
            http("Refresh Token")
                .post("/auth/refresh")
                .body(StringBody { session ->
                    """{"refreshToken": "${session.getString("refreshToken")}"}"""
                })
                .check(status().`is`(200))
                .check(jsonPath("$.tokenResponse.accessToken").saveAs("newAccessToken"))
                .check(jsonPath("$.tokenResponse.refreshToken").saveAs("newRefreshToken"))
        )
        .exec(
            http("Delete User")
                .delete("/auth/user")
                .header("Authorization") { session -> "Bearer ${session.getString("newAccessToken")}" }
                .check(status().`is`(200))
        )

    init {
        setUp(
            authScenario.injectOpen(rampUsers(100).during(Duration.ofSeconds(10)))
        ).protocols(httpProtocol)
    }
}