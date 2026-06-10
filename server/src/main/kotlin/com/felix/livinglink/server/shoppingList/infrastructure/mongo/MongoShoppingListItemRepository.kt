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
        val filters =
            buildList {
                add(Filters.eq("groupId", query.groupId))
                query.completed?.let { completed ->
                    add(Filters.eq("completed", completed))
                }
            }

        val sort =
            when (query.sort) {
                ShoppingListItemSort.CreatedAtAscending -> Sorts.ascending("createdAt")
                ShoppingListItemSort.CreatedAtDescending -> Sorts.descending("createdAt")
                ShoppingListItemSort.UpdatedAtAscending -> Sorts.ascending("updatedAt")
                ShoppingListItemSort.UpdatedAtDescending -> Sorts.descending("updatedAt")
                ShoppingListItemSort.NameAscending -> Sorts.ascending("name")
                ShoppingListItemSort.NameDescending -> Sorts.descending("name")
            }

        return collection
            .find(Filters.and(filters))
            .sort(sort)
            .skip(query.offset)
            .limit(query.limit)
            .toList()
            .map { it.toDomain() }
    }
}
