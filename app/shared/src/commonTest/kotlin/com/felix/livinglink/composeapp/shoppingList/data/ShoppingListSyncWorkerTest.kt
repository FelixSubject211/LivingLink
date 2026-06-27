package com.felix.livinglink.composeapp.shoppingList.data

import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository.ChangeCompleteStateResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListRepository.DeleteResult
import com.felix.livinglink.composeapp.shoppingList.domain.ShoppingListSyncLocalDataStore
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListSyncWorkerTest {

    private lateinit var baseRepository: ShoppingListRepository
    private lateinit var localDataStore: ShoppingListSyncLocalDataStore

    private lateinit var worker: ShoppingListSyncWorker

    private val pending = MutableStateFlow<List<ShoppingListPendingMutation>>(emptyList())

    @BeforeTest
    fun setUp() {
        baseRepository = mock()
        localDataStore = mock()

        every { localDataStore.pending } returns pending
        everySuspend { localDataStore.snapshot() } calls { pending.value }
        everySuspend { localDataStore.remove(any()) } calls { args ->
            val mutation = args.component1<ShoppingListPendingMutation>()
            pending.value = pending.value.filterNot { it == mutation }
        }

        worker = ShoppingListSyncWorker(
            baseRepository = baseRepository,
            shoppingListSyncLocalDataStore = localDataStore,
        )
    }

    private fun completeChange(
        itemId: String,
        completed: Boolean = true,
        groupId: String = "g1",
    ) = ShoppingListPendingMutation.CompleteChange(
        groupId = groupId,
        itemId = itemId,
        completed = completed,
    )

    private fun delete(
        itemId: String,
        groupId: String = "g1",
    ) = ShoppingListPendingMutation.Delete(
        groupId = groupId,
        itemId = itemId,
    )

    @Test
    fun `syncOnce returns true when nothing pending`() = runTest {
        everySuspend { localDataStore.snapshot() } returns emptyList()

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(0)) { baseRepository.changeItemCompleteState(any(), any()) }
        verifySuspend(exactly(0)) { baseRepository.deleteItem(any()) }
    }

    @Test
    fun `syncOnce applies a single complete change and returns true`() = runTest {
        everySuspend { localDataStore.snapshot() } returns listOf(completeChange("i1", completed = true))
        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.Success

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(1)) {
            baseRepository.changeItemCompleteState(itemId = "i1", completed = true)
        }
    }

    @Test
    fun `syncOnce applies a single delete and returns true`() = runTest {
        everySuspend { localDataStore.snapshot() } returns listOf(delete("i1"))
        everySuspend { baseRepository.deleteItem(any()) } returns DeleteResult.Success

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(1)) { baseRepository.deleteItem("i1") }
    }

    @Test
    fun `successful drain removes all superseded mutations`() = runTest {
        val m1 = completeChange("i1", completed = false)
        val m2 = completeChange("i1", completed = true)

        everySuspend { localDataStore.snapshot() } returns listOf(m1, m2)
        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.Success

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(1)) { baseRepository.changeItemCompleteState(any(), any()) }
        verifySuspend(exactly(1)) { localDataStore.remove(m1) }
        verifySuspend(exactly(1)) { localDataStore.remove(m2) }
    }

    @Test
    fun `coalesce picks delete over complete change for the same item`() = runTest {
        val complete = completeChange("i1", completed = true)
        val deletion = delete("i1")

        everySuspend { localDataStore.snapshot() } returns listOf(complete, deletion)
        everySuspend { baseRepository.deleteItem(any()) } returns DeleteResult.Success

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(1)) { baseRepository.deleteItem("i1") }
        verifySuspend(exactly(0)) { baseRepository.changeItemCompleteState(any(), any()) }
        verifySuspend(exactly(1)) { localDataStore.remove(complete) }
        verifySuspend(exactly(1)) { localDataStore.remove(deletion) }
    }

    @Test
    fun `coalesce picks the last delete when multiple deletes exist`() = runTest {
        val d1 = delete("i1")
        val d2 = delete("i1")

        everySuspend { localDataStore.snapshot() } returns listOf(d1, d2)
        everySuspend { baseRepository.deleteItem(any()) } returns DeleteResult.Success

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(1)) { baseRepository.deleteItem("i1") }
    }

    @Test
    fun `different items are applied independently`() = runTest {
        everySuspend { localDataStore.snapshot() } returns listOf(
            completeChange("i1", completed = true),
            delete("i2"),
        )
        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.Success
        everySuspend { baseRepository.deleteItem(any()) } returns DeleteResult.Success

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(1)) { baseRepository.changeItemCompleteState("i1", true) }
        verifySuspend(exactly(1)) { baseRepository.deleteItem("i2") }
    }

    @Test
    fun `conflict is treated as done and superseded are removed`() = runTest {
        val m = completeChange("i1", completed = true)

        everySuspend { localDataStore.snapshot() } returns listOf(m)
        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.Conflict

        assertTrue(worker.syncOnce())

        verifySuspend(exactly(1)) { localDataStore.remove(m) }
    }

    @Test
    fun `complete change network error returns false and keeps mutations`() = runTest {
        val m = completeChange("i1", completed = true)

        everySuspend { localDataStore.snapshot() } returns listOf(m)
        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.NetworkError

        assertFalse(worker.syncOnce())

        verifySuspend(exactly(0)) { localDataStore.remove(any()) }
    }

    @Test
    fun `complete change no active group returns false and keeps mutations`() = runTest {
        val m = completeChange("i1", completed = true)

        everySuspend { localDataStore.snapshot() } returns listOf(m)
        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.NoActiveGroup

        assertFalse(worker.syncOnce())

        verifySuspend(exactly(0)) { localDataStore.remove(any()) }
    }

    @Test
    fun `delete network error returns false and keeps mutations`() = runTest {
        val m = delete("i1")

        everySuspend { localDataStore.snapshot() } returns listOf(m)
        everySuspend { baseRepository.deleteItem(any()) } returns DeleteResult.NetworkError

        assertFalse(worker.syncOnce())

        verifySuspend(exactly(0)) { localDataStore.remove(any()) }
    }

    @Test
    fun `delete no active group returns false and keeps mutations`() = runTest {
        val m = delete("i1")

        everySuspend { localDataStore.snapshot() } returns listOf(m)
        everySuspend { baseRepository.deleteItem(any()) } returns DeleteResult.NoActiveGroup

        assertFalse(worker.syncOnce())

        verifySuspend(exactly(0)) { localDataStore.remove(any()) }
    }

    @Test
    fun `partial failure only removes superseded for the succeeded item`() = runTest {
        val ok = completeChange("i1", completed = true)
        val failing = delete("i2")

        everySuspend { localDataStore.snapshot() } returns listOf(ok, failing)
        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.Success
        everySuspend { baseRepository.deleteItem(any()) } returns DeleteResult.NetworkError

        assertFalse(worker.syncOnce())

        verifySuspend(exactly(1)) { localDataStore.remove(ok) }
        verifySuspend(exactly(0)) { localDataStore.remove(failing) }
    }

    @Test
    fun `run waits for pending then drains and clears the queue`() = runTest {
        val m = completeChange("i1", completed = true)

        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } returns
            ChangeCompleteStateResult.Success

        val job = backgroundScope.launchWorker()

        pending.value = listOf(m)
        runCurrent()

        verifySuspend(exactly(1)) {
            baseRepository.changeItemCompleteState("i1", true)
        }
        verifySuspend(exactly(1)) { localDataStore.remove(m) }
        assertTrue(pending.value.isEmpty())

        job.cancel()
    }

    @Test
    fun `run retries after the retry interval when a drain fails`() = runTest {
        val m = completeChange("i1", completed = true)

        everySuspend { baseRepository.changeItemCompleteState(any(), any()) } sequentially {
            returns(ChangeCompleteStateResult.NetworkError)
            returns(ChangeCompleteStateResult.Success)
        }

        val job = backgroundScope.launchWorker()

        pending.value = listOf(m)
        runCurrent()

        verifySuspend(exactly(0)) { localDataStore.remove(any()) }
        assertFalse(pending.value.isEmpty())

        advanceTimeBy(1_100)
        runCurrent()

        verifySuspend(exactly(1)) { localDataStore.remove(m) }
        assertTrue(pending.value.isEmpty())

        job.cancel()
    }

    private fun CoroutineScope.launchWorker() =
        launch { worker.run() }
}