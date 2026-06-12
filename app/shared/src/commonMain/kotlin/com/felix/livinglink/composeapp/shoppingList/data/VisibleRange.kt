package com.felix.livinglink.composeapp.shoppingList.data

data class VisibleRange(
    val first: Int,
    val last: Int,
) {
    fun toPages(totalCount: Int?, pageSize: Int, prefetch: Int): List<Int> {
        val firstPage = (first - prefetch).coerceAtLeast(0) / pageSize
        val unclampedLastPage = (last + prefetch) / pageSize
        val maxPage = totalCount?.let { ((it - 1).coerceAtLeast(0)) / pageSize }
        val lastPage = maxPage?.let { minOf(unclampedLastPage, it) } ?: unclampedLastPage
        return (firstPage..lastPage).toList()
    }
}