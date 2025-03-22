package felix.livinglink.auth

import felix.livinglink.common.ServerConfig
import felix.livinglink.common.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(
    config: ServerConfig,
    authService: AuthService
) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.registerUser(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.loginUser(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val response = authService.refreshAccessToken(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/logout") {
            val request = call.receive<LogoutRequest>()
            val response = authService.logoutUser(request)
            call.respond(HttpStatusCode.OK, response)
        }

        authenticate(config.authenticationConfig) {
            delete("/account") {
                val user = call.principal<UserPrincipal>()!!
                val response = authService.deleteUser(user.userId)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}