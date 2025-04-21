package felix.livinglink.event

import felix.livinglink.common.UserPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.eventRoutes(changeNotifier: ChangeNotifier) {
    route("/event") {
        get("/group-change") {
            val pollInterval = 20
            val userId = call.principal<UserPrincipal>()!!.userId
            val changeId = changeNotifier.getLastGroupChangeIdForUser(userId)
            call.respond(PollingUpdateResponse(changeId, pollInterval))
        }
    }
}