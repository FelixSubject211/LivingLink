package com.felix.livinglink.composeapp.shoppingList.network

import com.felix.livinglink.composeapp.config.BuildKonfig
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.core.network.getNetworkResult
import com.felix.livinglink.composeapp.core.network.postNetworkResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListPage
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRemoteDataSource
import com.felix.livinglink.shared.shoppingList.ChangeShoppingListItemCompleteStateRequestV1
import com.felix.livinglink.shared.shoppingList.ChangeShoppingListItemCompleteStateResponseV1
import com.felix.livinglink.shared.shoppingList.DeleteShoppingListItemRequestV1
import com.felix.livinglink.shared.shoppingList.DeleteShoppingListItemResponseV1
import com.felix.livinglink.shared.shoppingList.GetShoppingListItemsPageRequestV1
import com.felix.livinglink.shared.shoppingList.GetShoppingListItemsPageResponseV1
import com.felix.livinglink.shared.shoppingList.ShoppingListItemDtoV1
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import org.koin.core.annotation.Single

@Single(binds = [ShoppingListRemoteDataSource::class])
class KtorShoppingListRemoteDataSource(
    private val httpClient: HttpClient,
) : ShoppingListRemoteDataSource {
    override suspend fun getPage(
        apiKey: String,
        groupId: String,
        completed: Boolean?,
        limit: Int?,
        offset: String?,
    ): NetworkResult<ShoppingListPage> =
        httpClient.getNetworkResult<GetShoppingListItemsPageResponseV1>(
            "${BuildKonfig.BASE_URL}${GetShoppingListItemsPageRequestV1.ROUTE}"
        ) {
            bearerAuth(apiKey)
            parameter(GetShoppingListItemsPageRequestV1.QUERY_GROUP_ID, groupId)
            completed?.let { parameter(GetShoppingListItemsPageRequestV1.QUERY_COMPLETED, it) }
            limit?.let { parameter(GetShoppingListItemsPageRequestV1.QUERY_LIMIT, it) }
            offset?.let { parameter(GetShoppingListItemsPageRequestV1.QUERY_OFFSET, it) }
        }.map { response ->
            ShoppingListPage(
                items = response.items.map { it.toDomain() },
                nextCursor = response.nextCursor,
                totalCount = response.totalCount,
            )
        }

    override suspend fun changeItemCompleteState(
        apiKey: String,
        groupId: String,
        itemId: String,
        completed: Boolean,
    ): NetworkResult<ShoppingListItem?> =
        httpClient.postNetworkResult<ChangeShoppingListItemCompleteStateRequestV1, ChangeShoppingListItemCompleteStateResponseV1>(
            urlString = "${BuildKonfig.BASE_URL}${ChangeShoppingListItemCompleteStateRequestV1.ROUTE}",
            body = ChangeShoppingListItemCompleteStateRequestV1(
                groupId = groupId,
                itemId = itemId,
                completed = completed,
            ),
        ) {
            bearerAuth(apiKey)
        }.map { response ->
            when (response) {
                is ChangeShoppingListItemCompleteStateResponseV1.Changed ->
                    response.item.toDomain()

                is ChangeShoppingListItemCompleteStateResponseV1.AlreadyInState ->
                    response.item.toDomain()

                is ChangeShoppingListItemCompleteStateResponseV1.NotFound ->
                    null

                is ChangeShoppingListItemCompleteStateResponseV1.Conflict ->
                    null
            }
        }

    override suspend fun deleteItem(
        apiKey: String,
        groupId: String,
        itemId: String,
    ): NetworkResult<Boolean> =
        httpClient.postNetworkResult<DeleteShoppingListItemRequestV1, DeleteShoppingListItemResponseV1>(
            urlString = "${BuildKonfig.BASE_URL}${DeleteShoppingListItemRequestV1.ROUTE}",
            body = DeleteShoppingListItemRequestV1(
                groupId = groupId,
                itemId = itemId,
            ),
        ) {
            bearerAuth(apiKey)
        }.map { response ->
            when (response) {
                is DeleteShoppingListItemResponseV1.Deleted -> true
                is DeleteShoppingListItemResponseV1.NotFound -> false
            }
        }
}

private fun ShoppingListItemDtoV1.toDomain(): ShoppingListItem =
    ShoppingListItem(
        id = id,
        name = name,
        completed = completed,
        createdByUserId = createdByUserId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )