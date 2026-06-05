package com.felix.livinglink.server.user.delivery.http

import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.server.user.application.LoginUseCase
import com.felix.livinglink.shared.login.LoginRequest
import com.felix.livinglink.shared.login.LoginResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.core.annotation.Single

@Single(binds = [HttpRouteRegistrar::class])
class LoginHttpRoute(
    private val loginUseCase: LoginUseCase,
) : HttpRouteRegistrar {
    override fun register(route: Route) {
        route.post(LoginRequest.ROUTE) {
            val request = call.receive<LoginRequest>()

            val response =
                when (val output = loginUseCase(request.apiKey)) {
                    is LoginUseCase.Output.Valid ->
                        LoginResponse.Success(
                            userId = output.userId,
                            username = output.username,
                        )

                    is LoginUseCase.Output.Invalid ->
                        LoginResponse.InvalidKey
                }

            call.respond<LoginResponse>(HttpStatusCode.OK, response)
        }
    }
}
