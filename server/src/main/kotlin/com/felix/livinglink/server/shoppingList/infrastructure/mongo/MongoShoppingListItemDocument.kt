package com.felix.livinglink.server.shoppingList.infrastructure.mongo

import com.felix.livinglink.server.core.infrastructure.mongo.MongoVersionedDocument
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import kotlin.time.Instant

data class MongoShoppingListItemDocument(
    @param:BsonId
    override val id: String,
    val groupId: String,
    val name: String,
    val createdByUserId: String,
    val position: String,
    val completed: Boolean,
    val completionEvents: List<MongoCompletionEventDocument>,
    val createdAt: Instant,
    val updatedAt: Instant,
    @param:BsonProperty("version")
    override val version: Long,
) : MongoVersionedDocument<MongoShoppingListItemDocument> {
    override fun withVersion(version: Long): MongoShoppingListItemDocument =
        copy(version = version)

    fun toDomain(): ShoppingListItem =
        ShoppingListItem(
            id = id,
            groupId = groupId,
            name = name,
            createdByUserId = createdByUserId,
            position = position,
            completionEvents =
                completionEvents.map { event ->
                    event.toDomain()
                },
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version,
        )

    companion object {
        fun fromDomain(item: ShoppingListItem): MongoShoppingListItemDocument =
            MongoShoppingListItemDocument(
                id = item.id,
                groupId = item.groupId,
                name = item.name,
                createdByUserId = item.createdByUserId,
                position = item.position,
                completed = item.isCompleted,
                completionEvents =
                    item.completionEvents.map { event ->
                        MongoCompletionEventDocument.fromDomain(event)
                    },
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
                version = item.version,
            )
    }
}

data class MongoCompletionEventDocument(
    val byUserId: String,
    val completed: Boolean,
    val at: Instant,
) {
    fun toDomain(): ShoppingListItem.CompletionEvent =
        ShoppingListItem.CompletionEvent(
            byUserId = byUserId,
            completed = completed,
            at = at,
        )

    companion object {
        fun fromDomain(event: ShoppingListItem.CompletionEvent): MongoCompletionEventDocument =
            MongoCompletionEventDocument(
                byUserId = event.byUserId,
                completed = event.completed,
                at = event.at,
            )
    }
}
