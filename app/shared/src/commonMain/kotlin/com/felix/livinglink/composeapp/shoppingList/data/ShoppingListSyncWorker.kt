package com.felix.livinglink.composeapp.shoppingList.data

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository.ChangeCompleteStateResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository.DeleteResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListSyncLocalDataStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
class ShoppingListSyncWorker(
    @Named("base") private val baseRepository: ShoppingListRepository,
    private val shoppingListSyncLocalDataStore: ShoppingListSyncLocalDataStore,
) {
    suspend fun run() {
        while (true) {
            shoppingListSyncLocalDataStore.pending.first { it.isNotEmpty() }
            val drainedClean = drainOnce()
            if (!drainedClean) delay(RETRY_INTERVAL)
        }
    }

    suspend fun syncOnce(): Boolean {
        val pending = shoppingListSyncLocalDataStore.snapshot()
        if (pending.isEmpty()) return true
        return drainOnce()
    }

    private suspend fun drainOnce(): Boolean = coroutineScope {
        val plans = coalesce(shoppingListSyncLocalDataStore.snapshot())

        val results = plans
            .map { plan -> async { plan to apply(plan.mutation) } }
            .awaitAll()

        results
            .filter { (_, outcome) -> outcome == Outcome.Done }
            .forEach { (plan, _) ->
                plan.superseded.forEach({ shoppingListSyncLocalDataStore.remove(it) })
            }

        results.all { (_, outcome) -> outcome == Outcome.Done }
    }

    private fun coalesce(batch: List<ShoppingListPendingMutation>): List<MutationPlan> =
        batch
            .groupBy { it.itemId }
            .map { (_, mutations) -> MutationPlan(winner = pickWinner(mutations), superseded = mutations) }

    private fun pickWinner(mutations: List<ShoppingListPendingMutation>): ShoppingListPendingMutation =
        mutations.filterIsInstance<ShoppingListPendingMutation.Delete>().lastOrNull()
            ?: mutations.filterIsInstance<ShoppingListPendingMutation.CompleteChange>().last()

    private suspend fun apply(mutation: ShoppingListPendingMutation): Outcome =
        when (mutation) {
            is ShoppingListPendingMutation.CompleteChange ->
                baseRepository.changeItemCompleteState(
                    itemId = mutation.itemId,
                    completed = mutation.completed,
                ).toOutcome()

            is ShoppingListPendingMutation.Delete ->
                baseRepository.deleteItem(mutation.itemId).toOutcome()
        }

    private fun ChangeCompleteStateResult.toOutcome(): Outcome =
        when (this) {
            ChangeCompleteStateResult.Success,
            ChangeCompleteStateResult.Conflict ->
                Outcome.Done

            ChangeCompleteStateResult.NetworkError,
            ChangeCompleteStateResult.NoActiveGroup ->
                Outcome.RetryLater
        }

    private fun DeleteResult.toOutcome(): Outcome =
        when (this) {
            DeleteResult.Success ->
                Outcome.Done

            DeleteResult.NetworkError,
            DeleteResult.NoActiveGroup ->
                Outcome.RetryLater
        }

    private data class MutationPlan(
        val winner: ShoppingListPendingMutation,
        val superseded: List<ShoppingListPendingMutation>,
    ) {
        val mutation: ShoppingListPendingMutation get() = winner
    }

    private enum class Outcome { Done, RetryLater }

    private companion object {
        val RETRY_INTERVAL = 1.seconds
    }
}