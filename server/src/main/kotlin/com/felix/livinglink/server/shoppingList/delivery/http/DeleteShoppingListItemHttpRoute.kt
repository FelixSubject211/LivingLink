package com.felix.livinglink.server.shoppingList.delivery.http

import com.felix.livinglink.server.core.delivery.http.API_KEY_AUTH
import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.server.core.delivery.http.requireUser
import com.felix.livinglink.server.shoppingList.application.DeleteShoppingListItemsUseCase
import com.felix.livinglink.shared.shoppingList.DeleteShoppingListItemRequestV1
import com.felix.livinglink.shared.shoppingList.DeleteShoppingListItemResponseV1
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.core.annotation.Single

@Single(binds = [HttpRouteRegistrar::class])
class DeleteShoppingListItemHttpRoute(
    private val deleteShoppingListItemsUseCase: DeleteShoppingListItemsUseCase,
) : HttpRouteRegistrar {
    override fun register(route: Route) {
        route.authenticate(API_KEY_AUTH) {
            post(DeleteShoppingListItemRequestV1.ROUTE) {
                val user = requireUser()
                val request = call.receive<DeleteShoppingListItemRequestV1>()

                val output =
                    deleteShoppingListItemsUseCase(
                        DeleteShoppingListItemsUseCase.Input(
                            byUserId = user.id,
                            groupId = request.groupId,
                            idsToDelete = setOf(request.itemId),
                        ),
                    )

                require(
                    output.deletedIds.size + output.missingIds.size <= 1,
                ) { "Expected at most one result for a single item, got more." }

                val response =
                    when {
                        output.deletedIds.isNotEmpty() ->
                            DeleteShoppingListItemResponseV1.Deleted

                        else ->
                            DeleteShoppingListItemResponseV1.NotFound
                    }

                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
