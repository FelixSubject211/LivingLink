package felix.projekt.livinglink.composeApp.core.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * IMPORTANT:
 * If the structure of this sealed class changes,
 * the SQL function `erase_personal_data(jsonb)` in the server must be updated accordingly.
 */
@Serializable
sealed class PersonalData<out T> {
    @Serializable
    @SerialName("Present")
    data class Present<T>(val value: T) : PersonalData<T>()

    @Serializable
    @SerialName("Erased")
    data object Erased : PersonalData<Nothing>()
}

fun <T> PersonalData<T>.getOrNull(): T? {
    return when (this) {
        is PersonalData.Present -> {
            this.value
        }

        is PersonalData.Erased -> {
            null
        }
    }
}