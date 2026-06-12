package com.felix.livinglink.composeapp.shoppingList.network

import com.felix.livinglink.composeapp.config.BuildKonfig
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.core.network.getNetworkResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListPage
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRemoteDataSource
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
        cursor: String?,
    ): NetworkResult<ShoppingListPage> =
        httpClient.getNetworkResult<GetShoppingListItemsPageResponseV1>(
            "${BuildKonfig.BASE_URL}${GetShoppingListItemsPageRequestV1.ROUTE}"
        ) {
            bearerAuth(apiKey)
            parameter(GetShoppingListItemsPageRequestV1.QUERY_GROUP_ID, groupId)
            completed?.let { parameter(GetShoppingListItemsPageRequestV1.QUERY_COMPLETED, it) }
            limit?.let { parameter(GetShoppingListItemsPageRequestV1.QUERY_LIMIT, it) }
            cursor?.let { parameter(GetShoppingListItemsPageRequestV1.QUERY_CURSOR, it) }
        }.map { response ->
            ShoppingListPage(
                items = response.items.map { it.toDomain() },
                nextCursor = response.nextCursor,
                totalCount = response.totalCount,
            )
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