package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemQuery
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemSort
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
import kotlin.test.assertNull

class GetShoppingListItemsPageUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()

    private val useCase =
        GetShoppingListItemsPageUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
        )

    private fun items(count: Int): List<ShoppingListItem> =
        (1..count).map { shoppingListItem(id = "id-$it", groupId = "group-1", name = "item-$it") }

    @Test
    fun `fetches limit plus one and reports a next cursor when there are more items`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            val expectedQuery =
                ShoppingListItemQuery(
                    groupId = "group-1",
                    completed = false,
                    limit = 3,
                    offset = 0,
                    sort = ShoppingListItemSort.NameAscending,
                )

            everySuspend { shoppingListItemRepository.find(expectedQuery) } returns items(3)
            everySuspend { shoppingListItemRepository.count(expectedQuery) } returns 42

            val result =
                useCase(
                    GetShoppingListItemsPageUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        completed = false,
                        limit = 2,
                        offset = 0,
                        sort = ShoppingListItemSort.NameAscending,
                    ),
                )

            assertEquals(listOf("id-1", "id-2"), result.items.map { it.id })
            assertEquals(2, result.nextOffset)
            assertEquals(42, result.totalCount)
            verifySuspend(exactly(1)) { shoppingListItemRepository.find(expectedQuery) }
            verifySuspend(exactly(1)) { shoppingListItemRepository.count(expectedQuery) }
        }

    @Test
    fun `returns no next cursor when the page is not full`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            everySuspend { shoppingListItemRepository.find(any()) } returns items(2)
            everySuspend { shoppingListItemRepository.count(any()) } returns 2

            val result =
                useCase(
                    GetShoppingListItemsPageUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        completed = null,
                        limit = 5,
                        offset = 0,
                        sort = ShoppingListItemSort.CreatedAtDescending,
                    ),
                )

            assertEquals(2, result.items.size)
            assertNull(result.nextOffset)
            assertEquals(2, result.totalCount)
        }

    @Test
    fun `advances the offset by the page size for the next cursor`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit
            everySuspend { shoppingListItemRepository.find(any()) } returns items(3)
            everySuspend { shoppingListItemRepository.count(any()) } returns 100

            val result =
                useCase(
                    GetShoppingListItemsPageUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        completed = null,
                        limit = 2,
                        offset = 10,
                        sort = ShoppingListItemSort.CreatedAtDescending,
                    ),
                )

            assertEquals(12, result.nextOffset)
        }

    @Test
    fun `throws and queries nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(
                    GetShoppingListItemsPageUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        completed = null,
                        limit = 2,
                        offset = 0,
                        sort = ShoppingListItemSort.CreatedAtDescending,
                    ),
                )
            }

            verifySuspend(exactly(0)) { shoppingListItemRepository.find(any()) }
            verifySuspend(exactly(0)) { shoppingListItemRepository.count(any()) }
        }
}
