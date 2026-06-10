package com.felix.livinglink.server.group.delivery.http

import com.felix.livinglink.server.core.delivery.http.API_KEY_AUTH
import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.server.core.delivery.http.requireUser
import com.felix.livinglink.server.group.application.GetGroupsForUserUseCase
import com.felix.livinglink.shared.groups.GetGroupsForUserRequestV1
import com.felix.livinglink.shared.groups.GetGroupsForUserResponseV1
import com.felix.livinglink.shared.groups.GroupDtoV1
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.core.annotation.Single

@Single(binds = [HttpRouteRegistrar::class])
class GetGroupsForUserHttpRoute(
    private val getGroupsForUserUseCase: GetGroupsForUserUseCase,
) : HttpRouteRegistrar {
    override fun register(route: Route) {
        route.authenticate(API_KEY_AUTH) {
            get(GetGroupsForUserRequestV1.ROUTE) {
                val user = requireUser()

                val groups = getGroupsForUserUseCase(user.id)

                call.respond(
                    HttpStatusCode.OK,
                    GetGroupsForUserResponseV1(
                        groups = groups.map { GroupDtoV1(id = it.id, name = it.name) },
                    ),
                )
            }
        }
    }
}
