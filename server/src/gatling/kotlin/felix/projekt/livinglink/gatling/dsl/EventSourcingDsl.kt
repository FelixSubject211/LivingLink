package felix.projekt.livinglink.gatling.dsl

import felix.projekt.livinglink.gatling.common.SessionKeys
import felix.projekt.livinglink.gatling.common.authHeader
import felix.projekt.livinglink.shared.eventSourcing.requestModel.EventSourcingRequest
import felix.projekt.livinglink.shared.json
import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import okhttp3.internal.toLongOrDefault

fun appendEvent(groupIdKey: String, topic: String, payloadJsonString: String) =
    http("Append Event")
        .post("/event-sourcing/append")
        .header("Authorization") { session ->
            authHeader(session.getString(SessionKeys.accessToken)!!)
        }
        .body(StringBody { session ->
            json.encodeToString(
                EventSourcingRequest.Append(
                    groupId = session.getString(groupIdKey)!!,
                    topic = topic,
                    payload = json.parseToJsonElement(payloadJsonString),
                    expectedLastEventId = session.getString(SessionKeys.lastEventId)?.toLongOrDefault(0) ?: 0
                )
            )
        })
        .check(status().`is`(200))
        .check(jsonPath("$.event.eventId").saveAs(SessionKeys.lastEventId))

fun pollEvents(groupIdKey: String, topic: String) =
    http("Poll Events")
        .post("/event-sourcing/poll")
        .header("Authorization") { session ->
            authHeader(session.getString(SessionKeys.accessToken)!!)
        }
        .body(StringBody { session ->
            json.encodeToString(
                EventSourcingRequest.Poll(
                    groupId = session.getString(groupIdKey)!!,
                    topic = topic,
                    lastKnownEventId = session.getString(SessionKeys.lastEventId)?.toLongOrDefault(0) ?: 0
                )
            )
        })
        .check(status().`is`(200))
        .check(
            jsonPath("$.events[*].eventId").findAll().optional().saveAs(SessionKeys.fetchedEventIds)
        )
