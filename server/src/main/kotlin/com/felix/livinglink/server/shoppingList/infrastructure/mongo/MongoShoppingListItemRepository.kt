package com.felix.livinglink.server.shoppingList.infrastructure.mongo

import com.felix.livinglink.server.core.domain.CrudRepository
import com.felix.livinglink.server.core.domain.MappedCrudRepository
import com.felix.livinglink.server.core.infrastructure.mongo.MongoCrudRepository
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemSort
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
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
    override suspend fun find(query: ShoppingListItemQuery): List<ShoppingListItem> {
        val sort =
            when (query.sort) {
                ShoppingListItemSort.CreatedAtAscending ->
                    Sorts.orderBy(
                        Sorts.ascending("createdAt"),
                        Sorts.ascending("_id"),
                    )
                ShoppingListItemSort.CreatedAtDescending ->
                    Sorts.orderBy(
                        Sorts.descending("createdAt"),
                        Sorts.descending("_id"),
                    )
                ShoppingListItemSort.UpdatedAtAscending ->
                    Sorts.orderBy(
                        Sorts.ascending("updatedAt"),
                        Sorts.ascending("_id"),
                    )
                ShoppingListItemSort.UpdatedAtDescending ->
                    Sorts.orderBy(
                        Sorts.descending("updatedAt"),
                        Sorts.ascending("_id"),
                    )
                ShoppingListItemSort.NameAscending ->
                    Sorts.orderBy(
                        Sorts.ascending("name"),
                        Sorts.ascending("_id"),
                    )
                ShoppingListItemSort.NameDescending ->
                    Sorts.orderBy(
                        Sorts.descending("name"),
                        Sorts.ascending("_id"),
                    )
            }

        return collection
            .find(query.toFilter())
            .sort(sort)
            .skip(query.offset)
            .limit(query.limit)
            .toList()
            .map { it.toDomain() }
    }

    override suspend fun count(query: ShoppingListItemQuery): Long =
        collection.countDocuments(query.toFilter())

    private fun ShoppingListItemQuery.toFilter(): Bson {
        val filters =
            buildList {
                add(Filters.eq("groupId", groupId))
                completed?.let { add(Filters.eq("completed", it)) }
            }
        return Filters.and(filters)
    }
}
