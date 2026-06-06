package com.felix.livinglink.server.group.infrastructure.mongo

import org.bson.codecs.pojo.annotations.BsonId

data class MongoActiveMcpGroupDocument(
    @param:BsonId
    val userId: String,
    val groupId: String,
)
