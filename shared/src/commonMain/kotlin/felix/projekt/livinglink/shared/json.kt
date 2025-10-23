package felix.projekt.livinglink.shared

import kotlinx.serialization.json.Json

val json = Json {
    isLenient = true
    allowStructuredMapKeys = true
    classDiscriminator = "type"
}