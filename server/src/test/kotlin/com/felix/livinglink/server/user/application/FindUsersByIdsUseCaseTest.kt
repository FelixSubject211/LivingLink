package com.felix.livinglink.server.user.application

import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.user.application.FindUsersByIdsUseCase
import com.felix.livinglink.server.user.domain.UserProvider
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class FindUsersByIdsUseCaseTest {
    private val userProvider = mock<UserProvider>()

    private val useCase =
        FindUsersByIdsUseCase(
            userProvider = userProvider,
        )

    private val user1 =
        User(
            id = "1",
            username = "alice",
        )

    private val user2 =
        User(
            id = "2",
            username = "bob",
        )

    private val user3 =
        User(
            id = "3",
            username = "charlie",
        )

    @Test
    fun `returns only users matching requested ids`() {
        every { userProvider.usersById() } returns
            mapOf(
                "1" to user1,
                "2" to user2,
                "3" to user3,
            )

        val result = useCase(listOf("1", "3"))

        assertEquals(
            mapOf(
                "1" to user1,
                "3" to user3,
            ),
            result,
        )
    }

    @Test
    fun `ignores unknown ids`() {
        every { userProvider.usersById() } returns
            mapOf(
                "1" to user1,
            )

        val result = useCase(listOf("1", "999"))

        assertEquals(
            mapOf("1" to user1),
            result,
        )
    }

    @Test
    fun `returns empty map when no match`() {
        every { userProvider.usersById() } returns
            mapOf(
                "1" to user1,
            )

        val result = useCase(listOf("999"))

        assertEquals(emptyMap(), result)
    }
}
