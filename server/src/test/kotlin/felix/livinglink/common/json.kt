package felix.livinglink.common

import kotlinx.serialization.json.Json

val json = Json {
    isLenient = true
    allowStructuredMapKeys = true
}