package felix.livinglink.common

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface UuidFactory {
    operator fun invoke(): String
}

class UuidDefaultFactory : UuidFactory {
    @OptIn(ExperimentalUuidApi::class)
    override operator fun invoke() = Uuid.random().toString()
}