package com.felix.livinglink.server.auth.delivery.http

import com.felix.livinglink.server.auth.application.LoginUseCase
import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.shared.auth.LoginRequestV1
import com.felix.livinglink.shared.auth.LoginResponseV1
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
        route.post(LoginRequestV1.ROUTE) {
            val request = call.receive<LoginRequestV1>()

            val response =
                when (val output = loginUseCase(request.apiKey)) {
                    is LoginUseCase.Output.Valid ->
                        LoginResponseV1.Success(
                            userId = output.userId,
                            username = output.username,
                        )

                    is LoginUseCase.Output.Invalid ->
                        LoginResponseV1.InvalidKey
                }

            call.respond<LoginResponseV1>(HttpStatusCode.OK, response)
        }
    }
}
