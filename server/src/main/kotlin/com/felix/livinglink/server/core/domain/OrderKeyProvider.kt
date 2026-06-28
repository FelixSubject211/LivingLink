package com.felix.livinglink.server.core.domain

interface OrderKeyProvider {
    fun between(before: String?, after: String?): String

    fun nKeysBetween(before: String?, after: String?, count: Int): List<String>

    fun jitter(key: String): String
}
