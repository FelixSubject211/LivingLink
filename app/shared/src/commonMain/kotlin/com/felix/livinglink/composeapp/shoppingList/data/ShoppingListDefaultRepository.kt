package com.felix.livinglink.composeapp.shoppingList.data

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.core.domain.Loadable
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsRepository
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListContent
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListLocalDataSource
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRemoteDataSource
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class)
@Single(binds = [ShoppingListRepository::class])
class ShoppingListDefaultRepository(
    private val shoppingListRemoteDataSource: ShoppingListRemoteDataSource,
    private val shoppingListLocalDataSource: ShoppingListLocalDataSource,
    private val authRepository: AuthRepository,
    private val groupsRepository: GroupsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ShoppingListRepository {

    private val visibleRange = MutableStateFlow(VisibleRange(first = 0, last = 0))

    override val state: Flow<Loadable<ShoppingListContent>> =
        channelFlow {
            launch { evictRemovedGroups() }

            groupsRepository.selectedGroupId
                .distinctUntilChanged()
                .collectLatest { groupId ->
                    when (groupId) {
                        null -> send(Loadable.Loading)
                        else -> syncGroup(groupId)
                    }
                }
        }.flowOn(dispatcher)

    override fun setVisibleRange(firstVisibleIndex: Int, lastVisibleIndex: Int) {
        visibleRange.value = VisibleRange(first = firstVisibleIndex, last = lastVisibleIndex)
    }

    private suspend fun evictRemovedGroups() {
        groupsRepository.state.collect { loadable ->
            if (loadable is Loadable.Content) {
                shoppingListLocalDataSource.retainGroups(
                    loadable.value.groups.map(Group::id).toSet(),
                )
            }
        }
    }

    private suspend fun ProducerScope<Loadable<ShoppingListContent>>.syncGroup(groupId: String) =
        coroutineScope {
            val loader = PageLoader(
                groupId = groupId,
                scope = this,
                remoteDataSource = shoppingListRemoteDataSource,
                localDataSource = shoppingListLocalDataSource,
                authRepository = authRepository,
                visibleRange = visibleRange,
            )

            launch { loader.loadMissingPagesWhileScrolling() }
            launch { loader.refreshDesiredPagesPeriodically() }

            combine(
                shoppingListLocalDataSource.observe(groupId),
                loader.failedBeforeFirstData,
            ) { cached, failed ->
                when {
                    cached != null -> Loadable.Content(cached)
                    failed -> Loadable.Error.Network
                    else -> Loadable.Loading
                }
            }.collect { send(it) }
        }
}