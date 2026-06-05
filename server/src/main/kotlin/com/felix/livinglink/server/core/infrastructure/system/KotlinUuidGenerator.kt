package com.felix.livinglink.server.core.infrastructure.system

import com.felix.livinglink.server.core.domain.UuidGenerator
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single(binds = [UuidGenerator::class])
class KotlinUuidGenerator : UuidGenerator {
    @OptIn(ExperimentalUuidApi::class)
    override fun invoke(): String =
        Uuid.random().toString()
}
