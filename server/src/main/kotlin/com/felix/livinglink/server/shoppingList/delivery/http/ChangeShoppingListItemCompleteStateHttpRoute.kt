package com.felix.livinglink.server.shoppingList.delivery.http

import com.felix.livinglink.server.core.delivery.http.API_KEY_AUTH
import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.server.core.delivery.http.requireUser
import com.felix.livinglink.server.shoppingList.application.ChangeShoppingListItemsCompleteStateUseCase
import com.felix.livinglink.shared.shoppingList.ChangeShoppingListItemCompleteStateRequestV1
import com.felix.livinglink.shared.shoppingList.ChangeShoppingListItemCompleteStateResponseV1
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.core.annotation.Single

@Single(binds = [HttpRouteRegistrar::class])
class ChangeShoppingListItemCompleteStateHttpRoute(
    private val changeShoppingListItemsCompleteStateUseCase: ChangeShoppingListItemsCompleteStateUseCase,
) : HttpRouteRegistrar {
    override fun register(route: Route) {
        route.authenticate(API_KEY_AUTH) {
            post(ChangeShoppingListItemCompleteStateRequestV1.ROUTE) {
                val user = requireUser()
                val request = call.receive<ChangeShoppingListItemCompleteStateRequestV1>()

                val output =
                    changeShoppingListItemsCompleteStateUseCase(
                        ChangeShoppingListItemsCompleteStateUseCase.Input(
                            byUserId = user.id,
                            groupId = request.groupId,
                            changes =
                                listOf(
                                    ChangeShoppingListItemsCompleteStateUseCase.Change(
                                        itemId = request.itemId,
                                        completed = request.completed,
                                        at = request.at,
                                    ),
                                ),
                        ),
                    )

                require(
                    output.changedItems.size +
                        output.alreadyChangedItems.size +
                        output.missingIds.size +
                        output.conflictedIds.size <= 1,
                ) { "Expected at most one result for a single item, got more." }

                val response =
                    when {
                        output.changedItems.isNotEmpty() ->
                            ChangeShoppingListItemCompleteStateResponseV1.Changed(
                                output.changedItems.first().toDtoV1(),
                            )

                        output.alreadyChangedItems.isNotEmpty() ->
                            ChangeShoppingListItemCompleteStateResponseV1.AlreadyInState(
                                output.alreadyChangedItems.first().toDtoV1(),
                            )

                        output.conflictedIds.isNotEmpty() ->
                            ChangeShoppingListItemCompleteStateResponseV1.Conflict

                        else ->
                            ChangeShoppingListItemCompleteStateResponseV1.NotFound
                    }

                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
