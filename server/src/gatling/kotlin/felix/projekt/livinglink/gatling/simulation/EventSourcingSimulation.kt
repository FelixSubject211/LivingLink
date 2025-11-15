package felix.projekt.livinglink.gatling.simulation

import felix.projekt.livinglink.gatling.common.SessionKeys
import felix.projekt.livinglink.gatling.common.httpProtocol
import felix.projekt.livinglink.gatling.dsl.appendEvent
import felix.projekt.livinglink.gatling.dsl.createGroup
import felix.projekt.livinglink.gatling.dsl.deleteUser
import felix.projekt.livinglink.gatling.dsl.pollEvents
import felix.projekt.livinglink.gatling.dsl.randomUsername
import felix.projekt.livinglink.gatling.dsl.register
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import java.time.Duration

class EventSourcingSimulation : Simulation() {

    private val scenario = scenario("Event Sourcing Flow")
        .exec { it.set(SessionKeys.username1, randomUsername()) }
        .exec(register(usernameKey = SessionKeys.username1, password = "testpass"))
        .exec(createGroup("Group"))
        .repeat(5_000).on(
            exec(
                appendEvent(
                    groupIdKey = SessionKeys.groupId,
                    topic = "testTopic",
                    payloadJsonString = """{"message":"hello world"}"""
                )
            )
        )
        .exec { it.set(SessionKeys.lastEventId, null) }
        .repeat(10).on(
            exec(
                pollEvents(
                    groupIdKey = SessionKeys.groupId,
                    topic = "testTopic"
                )
            ).exec { session ->
                val ids = session.getList<String>(SessionKeys.fetchedEventIds)
                    .mapNotNull { it.toLongOrNull() }
                    .sorted()
                if (ids.isNotEmpty()) {
                    session.set(SessionKeys.lastEventId, ids.last())
                } else {
                    session
                }
            }
        )
        .exec(deleteUser())

    init {
        setUp(
            scenario.injectOpen(rampUsers(1).during(Duration.ofSeconds(1)))
        ).protocols(httpProtocol)
    }
}