package com.felix.livinglink.server.shoppingList.application

import com.felix.livinglink.server.core.domain.TimeProvider
import com.felix.livinglink.server.core.domain.stubConflict
import com.felix.livinglink.server.core.domain.stubDoesNotUpdate
import com.felix.livinglink.server.core.domain.stubNotFound
import com.felix.livinglink.server.core.domain.stubUpdates
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItem
import com.felix.livinglink.server.shoppingList.domain.ShoppingListItemRepository
import com.felix.livinglink.server.shoppingList.domain.completionEvent
import com.felix.livinglink.server.shoppingList.domain.shoppingListItem
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class ChangeShoppingListItemsCompleteStateUseCaseTest {
    private val shoppingListItemRepository = mock<ShoppingListItemRepository>()
    private val timeProvider = mock<TimeProvider>()

    private val useCase =
        ChangeShoppingListItemsCompleteStateUseCase(
            shoppingListItemRepository = shoppingListItemRepository,
            timeProvider = timeProvider,
        )

    private val now = Clock.System.now()

    private val openItem =
        shoppingListItem(
            id = "id-1",
            createdAt = now - 1.days,
        )

    private val completedItem =
        openItem.copy(
            completionEvents =
                listOf(
                    completionEvent(at = now - 1.hours),
                ),
        )

    @Test
    fun `completed items land in changedItems`() =
        runTest {
            every { timeProvider() } returns now

            shoppingListItemRepository.stubUpdates(
                id = "id-1",
                currentItem = openItem,
            )

            val expectedCompleted =
                openItem.complete(
                    byUserId = "user-1",
                    at = now,
                )

            val result =
                useCase(
                    ChangeShoppingListItemsCompleteStateUseCase.Input(
                        byUserId = "user-1",
                        idsToCompleteState =
                            mapOf(
                                "id-1" to true,
                            ),
                    ),
                )

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
            every { timeProvider() } returns now

            shoppingListItemRepository.stubUpdates(
                id = "id-1",
                currentItem = completedItem,
            )

            val expectedUncompleted =
                completedItem.unComplete(
                    byUserId = "user-1",
                    at = now,
                )

            val result =
                useCase(
                    ChangeShoppingListItemsCompleteStateUseCase.Input(
                        byUserId = "user-1",
                        idsToCompleteState =
                            mapOf(
                                "id-1" to false,
                            ),
                    ),
                )

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
            shoppingListItemRepository.stubDoesNotUpdate(
                id = "id-1",
                currentItem = completedItem,
            )

            val result =
                useCase(
                    ChangeShoppingListItemsCompleteStateUseCase.Input(
                        byUserId = "user-1",
                        idsToCompleteState =
                            mapOf(
                                "id-1" to true,
                            ),
                    ),
                )

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
    fun `already uncompleted items land in alreadyChangedItems`() =
        runTest {
            shoppingListItemRepository.stubDoesNotUpdate(
                id = "id-1",
                currentItem = openItem,
            )

            val result =
                useCase(
                    ChangeShoppingListItemsCompleteStateUseCase.Input(
                        byUserId = "user-1",
                        idsToCompleteState =
                            mapOf(
                                "id-1" to false,
                            ),
                    ),
                )

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = emptyList(),
                    alreadyChangedItems = listOf(openItem),
                    missingIds = emptyList(),
                    conflictedIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `missing items land in missingIds`() =
        runTest {
            shoppingListItemRepository.stubNotFound<ShoppingListItem>(id = "id-1")

            val result =
                useCase(
                    ChangeShoppingListItemsCompleteStateUseCase.Input(
                        byUserId = "user-1",
                        idsToCompleteState =
                            mapOf(
                                "id-1" to true,
                            ),
                    ),
                )

            assertEquals(
                ChangeShoppingListItemsCompleteStateUseCase.Output(
                    changedItems = emptyList(),
                    alreadyChangedItems = emptyList(),
                    missingIds = listOf("id-1"),
                    conflictedIds = emptyList(),
                ),
                result,
            )
        }

    @Test
    fun `conflicted items land in conflictedIds`() =
        runTest {
            shoppingListItemRepository.stubConflict<ShoppingListItem>(id = "id-1")

            val result =
                useCase(
                    ChangeShoppingListItemsCompleteStateUseCase.Input(
                        byUserId = "user-1",
                        idsToCompleteState =
                            mapOf(
                                "id-1" to true,
                            ),
                    ),
                )

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
}
