package felix.projekt.livinglink.gatling.dsl

import felix.projekt.livinglink.gatling.common.SessionKeys
import felix.projekt.livinglink.shared.auth.requestModel.AuthRequest
import felix.projekt.livinglink.shared.json
import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

fun register(usernameKey: String, password: String) = http("Register")
    .post("/auth/register")
    .body(StringBody { session ->
        json.encodeToString(
            AuthRequest.Register(
                session.getString(usernameKey)!!,
                password
            )
        )
    })
    .check(status().`is`(200))
    .check(jsonPath("$.tokenResponse.accessToken").saveAs(SessionKeys.accessToken))
    .check(jsonPath("$.tokenResponse.refreshToken").saveAs(SessionKeys.refreshToken))


fun login(usernameKey: String, password: String) = http("Login")
    .post("/auth/login")
    .body(StringBody { session ->
        json.encodeToString(
            AuthRequest.Login(
                session.getString(usernameKey)!!,
                password
            )
        )
    })
    .check(status().`is`(200))
    .check(jsonPath("$.tokenResponse.accessToken").saveAs(SessionKeys.accessToken))
    .check(jsonPath("$.tokenResponse.refreshToken").saveAs(SessionKeys.refreshToken))


fun refresh() = http("Refresh")
    .post("/auth/refresh")
    .body(StringBody { session ->
        json.encodeToString(
            AuthRequest.Refresh(
                session.getString(SessionKeys.refreshToken)!!
            )
        )
    })
    .check(status().`is`(200))
    .check(jsonPath("$.tokenResponse.accessToken").saveAs(SessionKeys.accessToken))
    .check(jsonPath("$.tokenResponse.refreshToken").saveAs(SessionKeys.refreshToken))


fun deleteUser() = http("Delete User")
    .delete("/auth/user")
    .header("Authorization") { session ->
        "Bearer " + session.getString(SessionKeys.accessToken)
    }
    .check(status().`is`(200))