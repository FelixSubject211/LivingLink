package com.felix.livinglink.server.shoppingList.infrastructure.mongo

import com.felix.livinglink.server.core.infrastructure.mongo.MongoClientProvider
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("shoppingListItems")
fun shoppingListItemMongoCollection(
    mongoClientProvider: MongoClientProvider,
): MongoCollection<MongoShoppingListItemDocument> =
    mongoClientProvider
        .database()
        .getCollection<MongoShoppingListItemDocument>("shopping_list_items")
