package com.felix.livinglink.server.core.domain

interface UuidGenerator {
    operator fun invoke(): String
}
