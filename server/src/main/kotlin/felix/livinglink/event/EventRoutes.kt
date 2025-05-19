package felix.livinglink.event

import felix.livinglink.Config
import felix.livinglink.common.UserPrincipal
import felix.livinglink.groups.GroupStore
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.eventRoutes(
    config: Config,
    changeNotifier: ChangeNotifier,
    groupStore: GroupStore
) {
    route("/event") {
        get("/group-change") {
            val principal = call.principal<UserPrincipal>()!!
            val userId = principal.userId
            val groupId = call.request.queryParameters["groupId"]

            val membershipChangeId = changeNotifier.getLastGroupChangeIdForUser(userId)

            if (groupId != null) {
                val inToken = principal.groupIds.contains(groupId)
                val inDatabase = if (!inToken) groupStore.isUserIdInGroup(userId, groupId) else true

                if (!inDatabase) {
                    return@get call.respond(
                        PollingUpdateResponse(
                            membershipChangeId = membershipChangeId,
                            latestEventId = null,
                            nextPollInSeconds = config.pollingIntervalSeconds
                        )
                    )
                }
            }

            val latestEventId: Long? = groupId?.let { changeNotifier.getLastEventIdForGroup(it) }

            call.respond(
                PollingUpdateResponse(
                    membershipChangeId = membershipChangeId,
                    latestEventId = latestEventId,
                    nextPollInSeconds = config.pollingIntervalSeconds
                )
            )
        }
    }
}

