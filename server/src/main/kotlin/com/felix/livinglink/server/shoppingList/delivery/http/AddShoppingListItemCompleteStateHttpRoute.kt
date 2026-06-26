package com.felix.livinglink.server.shoppingList.delivery.http

import com.felix.livinglink.server.core.delivery.http.API_KEY_AUTH
import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.server.core.delivery.http.requireUser
import com.felix.livinglink.server.shoppingList.application.AddShoppingListItemsUseCase
import com.felix.livinglink.shared.shoppingList.AddShoppingListItemRequestV1
import com.felix.livinglink.shared.shoppingList.AddShoppingListItemResponseV1
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.core.annotation.Single

@Single(binds = [HttpRouteRegistrar::class])
class AddShoppingListItemCompleteStateHttpRoute(
    private val addShoppingListItemsUseCase: AddShoppingListItemsUseCase,
) : HttpRouteRegistrar {
    override fun register(route: Route) {
        route.authenticate(API_KEY_AUTH) {
            post(AddShoppingListItemRequestV1.ROUTE) {
                val user = requireUser()
                val request = call.receive<AddShoppingListItemRequestV1>()

                val output =
                    addShoppingListItemsUseCase(
                        AddShoppingListItemsUseCase.Input(
                            byUserId = user.id,
                            groupId = request.groupId,
                            names = listOf(request.name),
                        ),
                    )

                require(
                    output.size == 1,
                ) { "Expected one result for a single item" }

                val response =
                    AddShoppingListItemResponseV1(
                        output.first().toDtoV1(),
                    )

                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
