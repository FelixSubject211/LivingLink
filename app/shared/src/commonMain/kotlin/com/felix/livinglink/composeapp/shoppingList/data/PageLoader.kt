package com.felix.livinglink.composeapp.shoppingList.data

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.AuthState
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListLocalDataSource
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class PageLoader(
    private val groupId: String,
    private val scope: CoroutineScope,
    private val remoteDataSource: ShoppingListRemoteDataSource,
    private val localDataSource: ShoppingListLocalDataSource,
    private val authRepository: AuthRepository,
    visibleRange: StateFlow<VisibleRange>,
    private val reloadRequests: Flow<Int>,
    private val reloadVisibleRequests: Flow<Unit>,
) {
    val failedBeforeFirstData = MutableStateFlow(false)

    private val inFlightPages = MutableStateFlow<Set<Int>>(emptySet())

    private val cached: StateFlow<ShoppingListContent?> =
        localDataSource.observe(groupId)
            .stateIn(scope, SharingStarted.Eagerly, null)

    private val desiredPages: StateFlow<List<Int>> =
        combine(visibleRange, cached) { range, snapshot ->
            range.toPages(
                totalCount = snapshot?.totalCount,
                pageSize = PAGE_SIZE,
                prefetch = PREFETCH
            )
        }
            .distinctUntilChanged()
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = visibleRange.value.toPages(
                    totalCount = null,
                    pageSize = PAGE_SIZE,
                    prefetch = PREFETCH,
                ),
            )

    suspend fun loadMissingPagesWhileScrolling() {
        desiredPages.collect { pages ->
            pages.filterNot(::isCached).forEach(::launchLoad)
        }
    }

    suspend fun refreshDesiredPagesPeriodically() {
        while (true) {
            desiredPages.value.forEach(::launchLoad)
            delay(POLL_INTERVAL)
        }
    }

    suspend fun reloadRequestedPages() {
        reloadRequests
            .filterNotNull()
            .collect { index ->
                val page = index / PAGE_SIZE
                launchLoad(page)
            }
    }

    suspend fun reloadVisiblePagesOnRequest() {
        reloadVisibleRequests.collect {
            desiredPages.value.forEach(::launchLoad)
        }
    }

    private fun isCached(page: Int): Boolean {
        val snapshot = cached.value ?: return false
        if (snapshot.totalCount == 0) return true
        return snapshot.isLoadedAt(page * PAGE_SIZE)
    }

    private fun launchLoad(page: Int) {
        if (!markInFlight(page)) return

        scope.launch {
            try {
                loadPage(page)
            } finally {
                inFlightPages.update { it - page }
            }
        }
    }

    private suspend fun loadPage(page: Int) {
        val apiKey =
            (authRepository.authState.value as? AuthState.LoggedIn)?.apiKey ?: return

        val result =
            remoteDataSource.getPage(
                apiKey = apiKey,
                groupId = groupId,
                limit = PAGE_SIZE,
                offset = (page * PAGE_SIZE).toString(),
            )

        when (result) {
            is NetworkResult.Success -> {
                failedBeforeFirstData.value = false
                localDataSource.putRange(
                    groupId = groupId,
                    fromIndex = page * PAGE_SIZE,
                    items = result.value.items,
                    totalCount = result.value.totalCount,
                )
            }

            is NetworkResult.NetworkError ->
                failedBeforeFirstData.value = cached.value == null

            is NetworkResult.Unauthorized ->
                authRepository.clear()
        }
    }

    private fun markInFlight(page: Int): Boolean {
        while (true) {
            val current = inFlightPages.value
            if (page in current) return false
            if (inFlightPages.compareAndSet(current, current + page)) return true
        }
    }

    private companion object {
        const val PAGE_SIZE = 200
        const val PREFETCH = 100
        val POLL_INTERVAL = 5.seconds
    }
}