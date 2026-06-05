package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.DeleteResult
import com.felix.livinglink.server.shoppingList.application.DeleteShoppingListItemsUseCase
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteShoppingListItemsUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()

    private val useCase =
        DeleteShoppingListItemsUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
        )

    @Test
    fun `deleted items land in deletedIds`() =
        runTest {
            everySuspend { shoppingListItemRepository.deleteById("id-1") } returns DeleteResult.Deleted
            everySuspend { shoppingListItemRepository.deleteById("id-2") } returns DeleteResult.Deleted

            val result =
                useCase(
                    DeleteShoppingListItemsUseCase.Input(
                        idsToDelete = setOf("id-1", "id-2"),
                    ),
                )

            assertEquals(
                DeleteShoppingListItemsUseCase.Output(
                    deletedIds = listOf("id-1", "id-2"),
                    missingIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `missing items land in missingIds`() =
        runTest {
            everySuspend { shoppingListItemRepository.deleteById("id-1") } returns DeleteResult.NotFound

            val result =
                useCase(
                    DeleteShoppingListItemsUseCase.Input(
                        idsToDelete = setOf("id-1"),
                    ),
                )

            assertEquals(
                DeleteShoppingListItemsUseCase.Output(
                    deletedIds = emptyList(),
                    missingIds = listOf("id-1"),
                ),
                result,
            )
        }

    @Test
    fun `mixed results split correctly into deleted and missing`() =
        runTest {
            everySuspend { shoppingListItemRepository.deleteById("id-1") } returns DeleteResult.Deleted
            everySuspend { shoppingListItemRepository.deleteById("id-2") } returns DeleteResult.NotFound

            val result =
                useCase(
                    DeleteShoppingListItemsUseCase.Input(
                        idsToDelete = setOf("id-1", "id-2"),
                    ),
                )

            assertEquals(
                DeleteShoppingListItemsUseCase.Output(
                    deletedIds = listOf("id-1"),
                    missingIds = listOf("id-2"),
                ),
                result,
            )
        }
}
