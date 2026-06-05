package com.felix.livinglink.server.core.config

import org.koin.core.annotation.Single

@Single
class HttpTransportSettings {
    val httpHost: String by lazy {
        Env.required("LIVINGLINK_HTTP_HOST")
    }

    val httpPort: Int by lazy {
        Env.requiredInt("LIVINGLINK_HTTP_PORT")
    }
}
