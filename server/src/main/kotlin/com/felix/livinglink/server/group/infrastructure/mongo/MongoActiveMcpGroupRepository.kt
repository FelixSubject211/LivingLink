package com.felix.livinglink.server.group.infrastructure.mongo

import com.felix.livinglink.server.group.domain.ActiveMcpGroupRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [ActiveMcpGroupRepository::class])
class MongoActiveMcpGroupRepository(
    @Named("activeMcpGroups")
    private val collection: MongoCollection<MongoActiveMcpGroupDocument>,
) : ActiveMcpGroupRepository {
    override suspend fun getActiveMcpGroupId(userId: String): String? =
        collection
            .find(Filters.eq("_id", userId))
            .firstOrNull()
            ?.groupId

    override suspend fun setActiveMcpGroupId(userId: String, groupId: String) {
        collection.replaceOne(
            Filters.eq("_id", userId),
            MongoActiveMcpGroupDocument(userId = userId, groupId = groupId),
            ReplaceOptions().upsert(true),
        )
    }
}
