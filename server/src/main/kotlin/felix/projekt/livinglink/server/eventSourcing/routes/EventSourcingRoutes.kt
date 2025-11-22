package felix.projekt.livinglink.server.eventSourcing.routes

import felix.projekt.livinglink.server.core.routes.userId
import felix.projekt.livinglink.server.eventSourcing.config.EventSourcingConfig
import felix.projekt.livinglink.server.eventSourcing.domain.AppendEventResult
import felix.projekt.livinglink.server.eventSourcing.domain.PollEventsResult
import felix.projekt.livinglink.server.eventSourcing.interfaces.AppendEventUseCase
import felix.projekt.livinglink.server.eventSourcing.interfaces.PollEventsUseCase
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingRequest
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingResponse.AppendEvent
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingResponse.AppendEvent.VersionMismatch
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingResponse.Event
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingResponse.PollEvents
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingResponse.PollEvents.NotModified
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.eventSourcingRoutes(
    config: EventSourcingConfig,
    appendEventUseCase: AppendEventUseCase,
    pollEventsUseCase: PollEventsUseCase
) {
    route("/event-sourcing") {
        post("/append") {
            val request: EventSourcingRequest.Append = call.receive()
            when (
                val result = appendEventUseCase(
                    userId = call.userId,
                    groupId = request.groupId,
                    topic = request.topic,
                    payload = request.payload,
                    expectedLastEventId = request.expectedLastEventId
                )
            ) {
                is AppendEventResult.Success -> {
                    call.respond<AppendEvent>(
                        AppendEvent.Success(
                            event = Event(
                                eventId = result.event.eventId,
                                groupId = result.event.groupId,
                                topic = result.event.topic,
                                createdBy = result.event.createdBy,
                                createdAtEpochMillis = result.event.createdAtEpochMillis,
                                payload = result.event.payload
                            )
                        )
                    )
                }

                is AppendEventResult.VersionMismatch -> {
                    call.respond<AppendEvent>(
                        VersionMismatch
                    )
                }

                AppendEventResult.NotAuthorized -> {
                    call.respond<AppendEvent>(
                        AppendEvent.NotAuthorized
                    )
                }
            }
        }

        post("/poll") {
            val request: EventSourcingRequest.Poll = call.receive()
            val result = pollEventsUseCase(
                userId = call.userId,
                groupId = request.groupId,
                topic = request.topic,
                lastKnownEventId = request.lastKnownEventId
            )
            when (result) {
                is PollEventsResult.Success -> {
                    val events = result.events.map { event ->
                        Event(
                            eventId = event.eventId,
                            groupId = event.groupId,
                            topic = event.topic,
                            createdBy = event.createdBy,
                            createdAtEpochMillis = event.createdAtEpochMillis,
                            payload = event.payload
                        )
                    }

                    val nextPollAfterMillis = if (result.totalEvents > result.events.size) {
                        0L
                    } else {
                        config.defaultPollAfterMillis
                    }

                    call.respond<PollEvents>(
                        PollEvents.Success(
                            events = events,
                            totalEvents = result.totalEvents,
                            nextPollAfterMillis = nextPollAfterMillis
                        )
                    )
                }

                is PollEventsResult.NotModified -> {
                    call.respond<PollEvents>(
                        NotModified(
                            nextPollAfterMillis = config.notModifiedPollAfterMillis
                        )
                    )
                }

                PollEventsResult.NotAuthorized -> {
                    call.respond<PollEvents>(
                        PollEvents.NotAuthorized
                    )
                }
            }
        }
    }
}
