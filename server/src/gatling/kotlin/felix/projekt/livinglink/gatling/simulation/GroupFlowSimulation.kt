package felix.projekt.livinglink.gatling.simulation

import felix.projekt.livinglink.gatling.common.SessionKeys
import felix.projekt.livinglink.gatling.common.httpProtocol
import felix.projekt.livinglink.gatling.dsl.createGroup
import felix.projekt.livinglink.gatling.dsl.createInviteCode
import felix.projekt.livinglink.gatling.dsl.deleteInviteCode
import felix.projekt.livinglink.gatling.dsl.deleteUser
import felix.projekt.livinglink.gatling.dsl.getGroups
import felix.projekt.livinglink.gatling.dsl.joinGroup
import felix.projekt.livinglink.gatling.dsl.login
import felix.projekt.livinglink.gatling.dsl.randomUsername
import felix.projekt.livinglink.gatling.dsl.register
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation

class GroupFlowSimulation : Simulation() {

    private val groupScenario = scenario("Group Flow")
        .exec { it.set(SessionKeys.username1, randomUsername()) }
        .exec(register(usernameKey = SessionKeys.username1, password = "testpass"))
        .exec(createGroup(groupName = "MyGroup"))
        .exec(createInviteCode(groupIdKey = SessionKeys.groupId, inviteName = "Invite1"))

        .exec { it.set(SessionKeys.username2, randomUsername()) }
        .exec(register(usernameKey = SessionKeys.username2, password = "testpass"))
        .exec(joinGroup(inviteKeySession = SessionKeys.inviteCode))
        .exec(deleteUser())

        .exec(login(usernameKey = SessionKeys.username1, password = "testpass"))
        .exec(deleteInviteCode(groupIdKey = SessionKeys.groupId, inviteCodeIdKey = SessionKeys.inviteCode))
        .repeat(10).on(
            exec(getGroups())
        )
        .exec(deleteUser())

    init {
        setUp(
            groupScenario.injectOpen(rampUsers(100).during(java.time.Duration.ofSeconds(10)))
        ).protocols(httpProtocol)
    }
}