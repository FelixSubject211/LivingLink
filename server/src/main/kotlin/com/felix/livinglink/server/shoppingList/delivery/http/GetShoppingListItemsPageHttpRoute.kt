package com.felix.livinglink.server.shoppingList.delivery.http

import com.felix.livinglink.server.core.delivery.http.API_KEY_AUTH
import com.felix.livinglink.server.core.delivery.http.HttpRouteRegistrar
import com.felix.livinglink.server.core.delivery.http.requireUser
import com.felix.livinglink.server.shoppingList.application.GetShoppingListItemsPageUseCase
import com.felix.livinglink.shared.shoppingList.GetShoppingListItemsPageRequestV1
import com.felix.livinglink.shared.shoppingList.GetShoppingListItemsPageResponseV1
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.core.annotation.Single

@Single(binds = [HttpRouteRegistrar::class])
class GetShoppingListItemsPageHttpRoute(
    private val getShoppingListItemsPageUseCase: GetShoppingListItemsPageUseCase,
) : HttpRouteRegistrar {
    override fun register(route: Route) {
        route.authenticate(API_KEY_AUTH) {
            get(GetShoppingListItemsPageRequestV1.ROUTE) {
                val user = requireUser()
                val params = call.request.queryParameters

                val groupId = params[GetShoppingListItemsPageRequestV1.QUERY_GROUP_ID]
                if (groupId.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing required query parameter '${GetShoppingListItemsPageRequestV1.QUERY_GROUP_ID}'.",
                    )
                    return@get
                }

                val completed =
                    params[GetShoppingListItemsPageRequestV1.QUERY_COMPLETED]
                        ?.toBooleanStrictOrNull()

                val limit =
                    params[GetShoppingListItemsPageRequestV1.QUERY_LIMIT]
                        ?.toIntOrNull()
                        ?.coerceIn(1, GetShoppingListItemsPageRequestV1.MAX_LIMIT)
                        ?: GetShoppingListItemsPageRequestV1.DEFAULT_LIMIT

                val offset =
                    params[GetShoppingListItemsPageRequestV1.QUERY_OFFSET]
                        ?.toIntOrNull()
                        ?.takeIf { it >= 0 }
                        ?: 0

                val output =
                    getShoppingListItemsPageUseCase(
                        GetShoppingListItemsPageUseCase.Input(
                            byUserId = user.id,
                            groupId = groupId,
                            completed = completed,
                            limit = limit,
                            offset = offset,
                        ),
                    )

                call.respond(
                    HttpStatusCode.OK,
                    GetShoppingListItemsPageResponseV1(
                        items = output.items.map { it.toDtoV1() },
                        totalCount = output.totalCount.toInt(),
                    ),
                )
            }
        }
    }
}
