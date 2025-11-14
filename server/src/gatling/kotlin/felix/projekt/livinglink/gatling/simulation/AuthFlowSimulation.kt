package felix.projekt.livinglink.gatling.simulation

import felix.projekt.livinglink.gatling.common.httpProtocol
import felix.projekt.livinglink.gatling.dsl.deleteUser
import felix.projekt.livinglink.gatling.dsl.login
import felix.projekt.livinglink.gatling.dsl.randomUsername
import felix.projekt.livinglink.gatling.dsl.refresh
import felix.projekt.livinglink.gatling.dsl.register
import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import java.time.Duration

class AuthFlowSimulation : Simulation() {
    private val authScenario = scenario("Full Auth Flow")
        .exec { it.set("usernameKey", randomUsername()) }
        .exec(register(usernameKey = "usernameKey", password = "testpass"))
        .exec(login(usernameKey = "usernameKey", password = "testpass"))
        .exec(refresh())
        .exec(deleteUser())

    init {
        setUp(
            authScenario.injectOpen(rampUsers(100).during(Duration.ofSeconds(10)))
        ).protocols(httpProtocol)
    }
}