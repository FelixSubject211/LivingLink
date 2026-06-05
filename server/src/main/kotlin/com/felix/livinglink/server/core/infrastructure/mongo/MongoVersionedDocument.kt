package com.felix.livinglink.server.core.infrastructure.mongo

interface MongoVersionedDocument<TDocument : MongoVersionedDocument<TDocument>> {
    val id: String

    val version: Long

    fun withVersion(version: Long): TDocument
}
