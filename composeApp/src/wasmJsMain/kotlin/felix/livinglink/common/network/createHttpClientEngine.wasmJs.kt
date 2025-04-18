package felix.livinglink.common.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createHttpClientEngine(): HttpClientEngine {
    return Js.create()
}