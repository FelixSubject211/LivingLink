package com.felix.livinglink.server.core.infrastructure.mongo

import com.felix.livinglink.server.core.config.MongoSettings
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries.fromCodecs
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.koin.core.annotation.Single

@Single
class MongoClientProvider(
    private val mongoSettings: MongoSettings,
) {
    private val client: MongoClient by lazy {
        MongoClient.create(clientSettings())
    }

    fun database(): MongoDatabase =
        client.getDatabase(mongoSettings.databaseName)

    fun close() {
        client.close()
    }

    private fun clientSettings(): MongoClientSettings {
        val codecRegistry =
            fromRegistries(
                fromCodecs(KotlinInstantCodec()),
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(
                    PojoCodecProvider
                        .builder()
                        .automatic(true)
                        .build(),
                ),
            )

        return MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(mongoSettings.connectionString))
            .codecRegistry(codecRegistry)
            .build()
    }
}
