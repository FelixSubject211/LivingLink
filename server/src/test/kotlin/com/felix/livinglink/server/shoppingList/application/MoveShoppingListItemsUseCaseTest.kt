package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.OrderKeyProvider
import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.stubConflict
import com.felix.livinglink.server.core.domain.stubUpdates
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
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

class MoveShoppingListItemsUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()
    private val orderKeyProvider = mock<OrderKeyProvider>()
    private val timeProvider = mock<TimeProvider>()

    private val useCase =
        MoveShoppingListItemsUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
            orderKeyProvider = orderKeyProvider,
            timeProvider = timeProvider,
        )

    private val now = Clock.System.now()

    private val itemA =
        shoppingListItem(
            id = "id-a",
            groupId = "group-1",
            name = "Milk",
            position = "a2",
            createdAt = now - 2.days,
        )

    private val itemB =
        shoppingListItem(
            id = "id-b",
            groupId = "group-1",
            name = "Bread",
            position = "a4",
            createdAt = now - 1.days,
        )

    private val itemC =
        shoppingListItem(
            id = "id-c",
            groupId = "group-1",
            name = "Eggs",
            position = "a6",
            createdAt = now - 1.days,
        )

    @Test
    fun `move after places item below the anchor`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            every { timeProvider() } returns now

            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA
            everySuspend { shoppingListItemRepository.findById("id-b") } returns itemB

            everySuspend {
                shoppingListItemRepository.findPositionBelow(
                    groupId = "group-1",
                    position = itemB.position,
                    excludingIds = setOf("id-a"),
                )
            } returns "a1"

            every { orderKeyProvider.between(before = "a1", after = "a4") } returns "a3"
            every { orderKeyProvider.jitter("a3") } returns "a3-jit"

            shoppingListItemRepository.stubUpdates(id = "id-a", currentItem = itemA)

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-a",
                                    afterId = "id-b",
                                ),
                            ),
                    ),
                )

            val expectedMoved = itemA.moveTo(position = "a3-jit", at = now)

            assertEquals(
                MoveShoppingListItemsUseCase.Output(
                    movedItems = listOf(expectedMoved),
                    missingIds = emptyList(),
                    anchorNotFoundIds = emptyList(),
                    conflictedIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `move before places item above the anchor`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            every { timeProvider() } returns now

            everySuspend { shoppingListItemRepository.findById("id-c") } returns itemC
            everySuspend { shoppingListItemRepository.findById("id-b") } returns itemB

            everySuspend {
                shoppingListItemRepository.findPositionAbove(
                    groupId = "group-1",
                    position = itemB.position,
                    excludingIds = setOf("id-c"),
                )
            } returns "a8"

            every { orderKeyProvider.between(before = "a4", after = "a8") } returns "a6"
            every { orderKeyProvider.jitter("a6") } returns "a6-jit"

            shoppingListItemRepository.stubUpdates(id = "id-c", currentItem = itemC)

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.Before(
                                    itemId = "id-c",
                                    beforeId = "id-b",
                                ),
                            ),
                    ),
                )

            val expectedMoved = itemC.moveTo(position = "a6-jit", at = now)

            assertEquals(
                MoveShoppingListItemsUseCase.Output(
                    movedItems = listOf(expectedMoved),
                    missingIds = emptyList(),
                    anchorNotFoundIds = emptyList(),
                    conflictedIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `moving an item to its own anchor is a no-op and lands in movedItems`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-a",
                                    afterId = "id-a",
                                ),
                            ),
                    ),
                )

            assertEquals(listOf(itemA), result.movedItems)
            assertEquals(emptyList(), result.missingIds)

            verifySuspend(exactly(0)) {
                shoppingListItemRepository.updateWithOptimisticLocking<ShoppingListItem>(
                    id = any(),
                    modify = any(),
                )
            }
        }

    @Test
    fun `missing item lands in missingIds`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-missing") } returns null

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-missing",
                                    afterId = "id-b",
                                ),
                            ),
                    ),
                )

            assertEquals(listOf("id-missing"), result.missingIds)
            assertEquals(emptyList(), result.movedItems)
        }

    @Test
    fun `item from another group lands in missingIds`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            val otherGroupItem =
                shoppingListItem(id = "id-other", groupId = "other-group", position = "a0")
            everySuspend { shoppingListItemRepository.findById("id-other") } returns otherGroupItem

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-other",
                                    afterId = "id-b",
                                ),
                            ),
                    ),
                )

            assertEquals(listOf("id-other"), result.missingIds)

            verifySuspend(exactly(0)) {
                shoppingListItemRepository.updateWithOptimisticLocking<ShoppingListItem>(
                    id = any(),
                    modify = any(),
                )
            }
        }

    @Test
    fun `missing anchor lands in anchorNotFoundIds`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA
            everySuspend { shoppingListItemRepository.findById("id-gone") } returns null

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-a",
                                    afterId = "id-gone",
                                ),
                            ),
                    ),
                )

            assertEquals(listOf("id-gone"), result.anchorNotFoundIds)
            assertEquals(emptyList(), result.movedItems)
        }

    @Test
    fun `anchor from another group lands in anchorNotFoundIds`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA

            val otherGroupAnchor =
                shoppingListItem(id = "id-anchor", groupId = "other-group", position = "a0")
            everySuspend { shoppingListItemRepository.findById("id-anchor") } returns otherGroupAnchor

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-a",
                                    afterId = "id-anchor",
                                ),
                            ),
                    ),
                )

            assertEquals(listOf("id-anchor"), result.anchorNotFoundIds)
        }

    @Test
    fun `optimistic locking conflict lands in conflictedIds`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            every { timeProvider() } returns now

            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA
            everySuspend { shoppingListItemRepository.findById("id-b") } returns itemB

            everySuspend {
                shoppingListItemRepository.findPositionBelow(
                    groupId = "group-1",
                    position = itemB.position,
                    excludingIds = setOf("id-a"),
                )
            } returns "a1"

            every { orderKeyProvider.between(before = "a1", after = "a4") } returns "a3"
            every { orderKeyProvider.jitter("a3") } returns "a3-jit"

            shoppingListItemRepository.stubConflict<ShoppingListItem>(id = "id-a")

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-a",
                                    afterId = "id-b",
                                ),
                            ),
                    ),
                )

            assertEquals(listOf("id-a"), result.conflictedIds)
            assertEquals(emptyList(), result.movedItems)
        }

    @Test
    fun `move after with no item below passes null as lower bound`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            every { timeProvider() } returns now

            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA
            everySuspend { shoppingListItemRepository.findById("id-b") } returns itemB

            everySuspend {
                shoppingListItemRepository.findPositionBelow(
                    groupId = "group-1",
                    position = itemB.position,
                    excludingIds = setOf("id-a"),
                )
            } returns null

            every { orderKeyProvider.between(before = null, after = "a4") } returns "a1"
            every { orderKeyProvider.jitter("a1") } returns "a1-jit"

            shoppingListItemRepository.stubUpdates(id = "id-a", currentItem = itemA)

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-a",
                                    afterId = "id-b",
                                ),
                            ),
                    ),
                )

            val expectedMoved = itemA.moveTo(position = "a1-jit", at = now)
            assertEquals(listOf(expectedMoved), result.movedItems)
        }

    @Test
    fun `move before with no item above passes null as upper bound`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            every { timeProvider() } returns now

            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA
            everySuspend { shoppingListItemRepository.findById("id-b") } returns itemB

            everySuspend {
                shoppingListItemRepository.findPositionAbove(
                    groupId = "group-1",
                    position = itemB.position,
                    excludingIds = setOf("id-a"),
                )
            } returns null

            every { orderKeyProvider.between(before = "a4", after = null) } returns "a7"
            every { orderKeyProvider.jitter("a7") } returns "a7-jit"

            shoppingListItemRepository.stubUpdates(id = "id-a", currentItem = itemA)

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.Before(
                                    itemId = "id-a",
                                    beforeId = "id-b",
                                ),
                            ),
                    ),
                )

            val expectedMoved = itemA.moveTo(position = "a7-jit", at = now)
            assertEquals(listOf(expectedMoved), result.movedItems)
        }

    @Test
    fun `multiple moves are applied in order and each result lands in the correct bucket`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            every { timeProvider() } returns now

            // Move 1: id-a after id-b → success
            everySuspend { shoppingListItemRepository.findById("id-a") } returns itemA
            everySuspend { shoppingListItemRepository.findById("id-b") } returns itemB

            everySuspend {
                shoppingListItemRepository.findPositionBelow(
                    groupId = "group-1",
                    position = itemB.position,
                    excludingIds = setOf("id-a"),
                )
            } returns "a1"

            every { orderKeyProvider.between(before = "a1", after = "a4") } returns "a3"
            every { orderKeyProvider.jitter("a3") } returns "a3-jit"

            shoppingListItemRepository.stubUpdates(id = "id-a", currentItem = itemA)

            // Move 2: id-missing after id-b → missing
            everySuspend { shoppingListItemRepository.findById("id-missing") } returns null

            // Move 3: id-c after id-gone → anchor not found
            everySuspend { shoppingListItemRepository.findById("id-c") } returns itemC
            everySuspend { shoppingListItemRepository.findById("id-gone") } returns null

            val result =
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(itemId = "id-a", afterId = "id-b"),
                                MoveShoppingListItemsUseCase.Move.After(itemId = "id-missing", afterId = "id-b"),
                                MoveShoppingListItemsUseCase.Move.After(itemId = "id-c", afterId = "id-gone"),
                            ),
                    ),
                )

            val expectedMoved = itemA.moveTo(position = "a3-jit", at = now)

            assertEquals(listOf(expectedMoved), result.movedItems)
            assertEquals(listOf("id-missing"), result.missingIds)
            assertEquals(listOf("id-gone"), result.anchorNotFoundIds)
            assertEquals(emptyList(), result.conflictedIds)
        }

    @Test
    fun `throws and moves nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(
                    MoveShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        moves =
                            listOf(
                                MoveShoppingListItemsUseCase.Move.After(
                                    itemId = "id-a",
                                    afterId = "id-b",
                                ),
                            ),
                    ),
                )
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
