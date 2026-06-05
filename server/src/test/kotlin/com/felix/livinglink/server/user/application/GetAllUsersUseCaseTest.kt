package com.felix.livinglink.server.user.application

import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.user.domain.UserProvider
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllUsersUseCaseTest {
    private val userProvider = mock<UserProvider>()

    private val useCase =
        GetAllUsersUseCase(
            userProvider = userProvider,
        )

    @Test
    fun `returns all users`() {
        val user1 =
            User(
                id = "1",
                username = "alice",
            )

        val user2 =
            User(
                id = "2",
                username = "bob",
            )

        every { userProvider.usersById() } returns
            mapOf(
                "1" to user1,
                "2" to user2,
            )

        val result = useCase()

        assertEquals(
            listOf(user1, user2),
            result,
        )
    }

    @Test
    fun `returns empty list when no users`() {
        every { userProvider.usersById() } returns emptyMap()

        val result = useCase()

        assertEquals(emptyList(), result)
    }
}
