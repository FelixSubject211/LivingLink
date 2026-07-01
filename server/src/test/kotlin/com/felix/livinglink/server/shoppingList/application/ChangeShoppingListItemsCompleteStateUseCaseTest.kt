package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.stubConflict
import com.felix.livinglink.server.core.domain.stubDoesNotUpdate
import com.felix.livinglink.server.core.domain.stubUpdates
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import com.felix.livinglink.server.shoppingList.domain.completionEvent
import com.felix.livinglink.server.shoppingList.domain.shoppingListItem
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class ChangeShoppingListItemsCompleteStateUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()

    private val useCase =
        ChangeShoppingListItemsCompleteStateUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
        )

    private val now = Clock.System.now()

    private val openItem =
        shoppingListItem(
            id = "id-1",
            groupId = "group-1",
            createdAt = now - 1.days,
        )

    private val completedItem =
        openItem.copy(
            completionEvents =
                listOf(
                    completionEvent(at = now - 1.hours),
                ),
        )

    private fun changeInput(
        itemId: String = "id-1",
        completed: Boolean,
        at: kotlin.time.Instant = now,
    ) = ChangeShoppingListItemsCompleteStateUseCase.Input(
        byUserId = "user-1",
        groupId = "group-1",
        changes =
            listOf(
                ChangeShoppingListItemsCompleteStateUseCase.Change(
                    itemId = itemId,
                    completed = completed,
                    at = at,
                ),
            ),
    )

    @Test
    fun `completed items land in changedItems`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-1") } returns openItem
            shoppingListItemRepository.stubUpdates(
                id = "id-1",
                currentItem = openItem,
            )

            val expectedCompleted =
                openItem.complete(
                    byUserId = "user-1",
                    at = now,
                )

            val result = useCase(changeInput(completed = true))

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = listOf(expectedCompleted),
                    alreadyChangedItems = emptyList(),
                    missingIds = emptyList(),
                    conflictedIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `uncompleted items land in changedItems`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-1") } returns completedItem
            shoppingListItemRepository.stubUpdates(
                id = "id-1",
                currentItem = completedItem,
            )

            val expectedUncompleted =
                completedItem.unComplete(
                    byUserId = "user-1",
                    at = now,
                )

            val result = useCase(changeInput(completed = false))

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = listOf(expectedUncompleted),
                    alreadyChangedItems = emptyList(),
                    missingIds = emptyList(),
                    conflictedIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `already completed items land in alreadyChangedItems`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-1") } returns completedItem
            shoppingListItemRepository.stubDoesNotUpdate(
                id = "id-1",
                currentItem = completedItem,
            )

            val result = useCase(changeInput(completed = true))

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = emptyList(),
                    alreadyChangedItems = listOf(completedItem),
                    missingIds = emptyList(),
                    conflictedIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `missing items land in missingIds`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-1") } returns null

            val result = useCase(changeInput(completed = true))

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = emptyList(),
                    alreadyChangedItems = emptyList(),
                    missingIds = listOf("id-1"),
                    conflictedIds = emptyList(),
                ),
                result,
            )

            verifySuspend(exactly(0)) {
                shoppingListItemRepository.updateWithOptimisticLocking<ShoppingListItem>(
                    id = any(),
                    modify = any(),
                )
            }
        }

    @Test
    fun `items from another group land in missingIds and are never updated`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-1") } returns
                shoppingListItem(id = "id-1", groupId = "other-group")

            val result = useCase(changeInput(completed = true))

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = emptyList(),
                    alreadyChangedItems = emptyList(),
                    missingIds = listOf("id-1"),
                    conflictedIds = emptyList(),
                ),
                result,
            )

            verifySuspend(exactly(0)) {
                shoppingListItemRepository.updateWithOptimisticLocking<ShoppingListItem>(
                    id = any(),
                    modify = any(),
                )
            }
        }

    @Test
    fun `conflicted items land in conflictedIds`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-1") } returns openItem
            shoppingListItemRepository.stubConflict<ShoppingListItem>(id = "id-1")

            val result = useCase(changeInput(completed = true))

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = emptyList(),
                    alreadyChangedItems = emptyList(),
                    missingIds = emptyList(),
                    conflictedIds = listOf("id-1"),
                ),
                result,
            )
        }

    @Test
    fun `throws and updates nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(changeInput(completed = true))
            }

            verifySuspend(exactly(0)) { shoppingListItemRepository.findById(any()) }
            verifySuspend(exactly(0)) {
                shoppingListItemRepository.updateWithOptimisticLocking<ShoppingListItem>(
                    id = any(),
                    modify = any(),
                )
            }
        }
}
