package com.felix.livinglink.server.core.domain

import kotlin.time.Instant

interface TimeProvider {
    operator fun invoke(): Instant
}
