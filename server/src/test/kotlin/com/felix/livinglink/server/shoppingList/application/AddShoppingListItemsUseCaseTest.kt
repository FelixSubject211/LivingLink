package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.group.application.RequireGroupMembershipUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import com.felix.livinglink.server.shoppingList.domain.shoppingListItem
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class AddShoppingListItemsUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()
    private val requireGroupMembershipUseCase = mock<RequireGroupMembershipUseCase>()

    private val useCase =
        AddShoppingListItemsUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
            requireGroupMembershipUseCase = requireGroupMembershipUseCase,
        )

    @Test
    fun `creates one item per NewItem with correct mapping`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } returns Unit

            everySuspend { shoppingListItemRepository.create(any()) } calls { (item: ShoppingListItem) -> item }

            val time1 = Clock.System.now()
            val time2 = time1 + 1.seconds

            val result =
                useCase(
                    AddShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        items =
                            listOf(
                                AddShoppingListItemsUseCase.NewItem(
                                    id = "id-1",
                                    name = "Milk",
                                    position = "a0-jit",
                                    createdAt = time1,
                                ),
                                AddShoppingListItemsUseCase.NewItem(
                                    id = "id-2",
                                    name = "Bread",
                                    position = "a1-jit",
                                    createdAt = time2,
                                ),
                            ),
                    ),
                )

            val expected =
                listOf(
                    shoppingListItem(
                        id = "id-1",
                        groupId = "group-1",
                        name = "Milk",
                        createdByUserId = "user-1",
                        position = "a0-jit",
                        createdAt = time1,
                        updatedAt = time1,
                    ),
                    shoppingListItem(
                        id = "id-2",
                        groupId = "group-1",
                        name = "Bread",
                        createdByUserId = "user-1",
                        position = "a1-jit",
                        createdAt = time2,
                        updatedAt = time2,
                    ),
                )

            assertEquals(expected, result)

            verify { requireGroupMembershipUseCase("user-1", "group-1") }
            verifySuspend(exactly(2)) { shoppingListItemRepository.create(any()) }
        }

    @Test
    fun `throws and creates nothing when the user is not a member of the group`() =
        runTest {
            every { requireGroupMembershipUseCase("user-1", "group-1") } throws
                IllegalArgumentException("User 'user-1' is not a member of group 'group-1'.")

            assertFailsWith<IllegalArgumentException> {
                useCase(
                    AddShoppingListItemsUseCase.Input(
                        byUserId = "user-1",
                        groupId = "group-1",
                        items =
                            listOf(
                                AddShoppingListItemsUseCase.NewItem(
                                    id = "id-1",
                                    name = "Milk",
                                    position = "a0",
                                    createdAt = Clock.System.now(),
                                ),
                            ),
                    ),
                )
            }

            verifySuspend(exactly(0)) { shoppingListItemRepository.create(any()) }
        }
}
