package felix.livinglink.change

import felix.livinglink.common.UserPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.changeRoutes(changeNotifier: ChangeNotifier) {
    route("/user") {
        get("/last-change") {
            val pollInterval = 30
            val userId = call.principal<UserPrincipal>()?.userId
                ?: return@get call.respond(
                    PollingUpdateResponse(
                        changeId = null,
                        nextPollInSeconds = 30
                    )
                )
            val changeId = changeNotifier.getLastChangeIdForUser(userId)
            call.respond(PollingUpdateResponse(changeId, pollInterval))
        }
    }
}