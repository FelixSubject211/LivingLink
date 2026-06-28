package com.felix.livinglink.server.shoppingList.infrastructure.mongo

import com.felix.livinglink.server.core.domain.CrudRepository
import com.felix.livinglink.server.core.domain.MappedCrudRepository
import com.felix.livinglink.server.core.infrastructure.mongo.MongoCrudRepository
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [ShoppingListItemRepository::class])
class MongoShoppingListItemRepository(
    @Named("shoppingListItems")
    private val collection: MongoCollection<MongoShoppingListItemDocument>,
) : ShoppingListItemRepository,
    CrudRepository<ShoppingListItem> by MappedCrudRepository(
        storageRepository =
            MongoCrudRepository(
                collection = collection,
                entityName = "Shopping list item",
            ),
        toStorage = MongoShoppingListItemDocument::fromDomain,
        toDomain = MongoShoppingListItemDocument::toDomain,
    ) {
    override suspend fun find(query: ShoppingListItemQuery): List<ShoppingListItem> =
        collection
            .find(query.toFilter())
            .sort(POSITION_SORT)
            .skip(query.offset)
            .limit(query.limit)
            .toList()
            .map { it.toDomain() }

    override suspend fun count(query: ShoppingListItemQuery): Long =
        collection.countDocuments(query.toFilter())

    override suspend fun findLastPosition(groupId: String): String? =
        collection
            .find(Filters.eq("groupId", groupId))
            .sort(Sorts.descending("position"))
            .limit(1)
            .firstOrNull()
            ?.position

    private fun ShoppingListItemQuery.toFilter(): Bson {
        val filters =
            buildList {
                add(Filters.eq("groupId", groupId))
                completed?.let { add(Filters.eq("completed", it)) }
            }
        return Filters.and(filters)
    }

    companion object {
        private val POSITION_SORT =
            Sorts.orderBy(
                Sorts.descending("position"),
                Sorts.descending("_id"),
            )
    }
}
