package com.felix.livinglink.server.group.infrastructure.mongo

import com.felix.livinglink.server.core.infrastructure.mongo.MongoClientProvider
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("activeMcpGroups")
fun activeMcpGroupMongoCollection(
    mongoClientProvider: MongoClientProvider,
): MongoCollection<MongoActiveMcpGroupDocument> =
    mongoClientProvider
        .database()
        .getCollection<MongoActiveMcpGroupDocument>("active_mcp_groups")
