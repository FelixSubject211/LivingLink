package felix.livinglink.eventSourcing

import felix.livinglink.common.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.eventSourcingRouts(
    eventSourcingService: EventSourcingService
) {
    route("/eventSourcing") {
        get("/events") {
            val userId = call.principal<UserPrincipal>()!!.userId

            val groupId = call.request.queryParameters["groupId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing groupId")

            val since = call.request.queryParameters["sinceEventIdExclusive"]?.toLongOrNull()

            val response = eventSourcingService.getEvents(
                request = GetEventSourcingEventsRequest(
                    groupId = groupId,
                    sinceEventIdExclusive = since
                ),
                userId = userId
            )
            call.respond(response)
        }

        post("append") {
            val userId = call.principal<UserPrincipal>()!!.userId
            val request = call.receive<AppendEventSourcingEventRequest>()
            val response = eventSourcingService.append(request, userId)
            call.respond(response)
        }
    }
}