package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
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

class DeleteShoppingListItemsUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()

    private val useCase =
        DeleteShoppingListItemsUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
        )

    @Test
    fun `deletes items that belong to the group, reports the rest as missing`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.findById("id-1") } returns
                shoppingListItem(id = "id-1", groupId = "group-1")
            everySuspend { shoppingListItemRepository.deleteById("id-1") } returns DeleteResult.Deleted

            everySuspend { shoppingListItemRepository.findById("id-2") } returns null

            everySuspend { shoppingListItemRepository.findById("id-3") } returns
                shoppingListItem(id = "id-3", groupId = "other-group")

            val result =
                useCase(
                    DeleteShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        idsToDelete = setOf("id-1", "id-2", "id-3"),
                    ),
                )

            assertEquals(listOf("id-1"), result.deletedIds)
            assertEquals(setOf("id-2", "id-3"), result.missingIds.toSet())

            verifySuspend(exactly(0)) { shoppingListItemRepository.deleteById("id-3") }
        }

    @Test
    fun `throws and deletes nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(
                    DeleteShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        idsToDelete = setOf("id-1"),
                    ),
                )
            }

            verifySuspend(exactly(0)) { shoppingListItemRepository.deleteById(any()) }
        }
}
