package felix.livinglink.event

import felix.livinglink.Config
import felix.livinglink.common.UserPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.eventRoutes(
    config: Config,
    changeNotifier: ChangeNotifier
) {
    route("/event") {
        get("/group-change") {
            val pollInterval = config.pollingIntervalSeconds
            val userId = call.principal<UserPrincipal>()!!.userId
            val changeId = changeNotifier.getLastGroupChangeIdForUser(userId)
            call.respond(PollingUpdateResponse(changeId, pollInterval))
        }
    }
}