package com.felix.livinglink.server.core.config

import org.koin.core.annotation.Single

@Single
class MongoSettings(
    connectionString: String? = null,
    databaseName: String? = null,
) {
    val connectionString: String = connectionString ?: Env.required("LIVINGLINK_MONGO_CONNECTION_STRING")
    val databaseName: String = databaseName ?: Env.required("LIVINGLINK_MONGO_DATABASE")
}
