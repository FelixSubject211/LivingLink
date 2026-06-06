package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
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

class ListShoppingListItemsUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()

    private val useCase =
        ListShoppingListItemsUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
        )

    private val item1 = shoppingListItem(id = "id-1", groupId = "group-1", name = "Milk")
    private val item2 = shoppingListItem(id = "id-2", groupId = "group-1", name = "Bread")

    @Test
    fun `builds a group-scoped query and returns the repository result`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            val expectedQuery =
                ShoppingListItemQuery(
                    groupId = "group-1",
                    completed = false,
                    limit = 50,
                    sort = ShoppingListItemSort.NameAscending,
                )

            everySuspend { shoppingListItemRepository.find(expectedQuery) } returns listOf(item1, item2)

            val result =
                useCase(
                    ListShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        completed = false,
                        limit = 50,
                        sort = ShoppingListItemSort.NameAscending,
                    ),
                )

            assertEquals(listOf(item1, item2), result)
            verifySuspend(exactly(1)) { shoppingListItemRepository.find(expectedQuery) }
        }

    @Test
    fun `throws and queries nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(
                    ListShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        completed = null,
                        limit = 50,
                        sort = ShoppingListItemSort.NameAscending,
                    ),
                )
            }

            verifySuspend(exactly(0)) { shoppingListItemRepository.find(any()) }
        }
}
