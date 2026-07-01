package com.felix.livinglink.server.core.domain

import com.felix.livinglink.shared.core.domain.FractionalIndexing
import org.koin.core.annotation.Single

@Single(binds = [OrderKeyProvider::class])
class FractionalIndexOrderKeyProvider : OrderKeyProvider {
    override fun between(before: String?, after: String?): String =
        FractionalIndexing.generateFractionalIndexBetween(before, after)

    override fun nKeysBetween(before: String?, after: String?, count: Int): List<String> {
        require(count >= 0) { "count must be >= 0" }
        if (count == 0) return emptyList()
        return FractionalIndexing.generateNFractionalIndicesBetween(before, after, count)
    }

    override fun jitter(key: String): String =
        key + FractionalIndexing.randomFractionSuffix(JITTER_LENGTH)

    companion object {
        private const val JITTER_LENGTH = 4
    }
}
