package com.felix.livinglink.server.user.application

import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.user.domain.UserProvider
import org.koin.core.annotation.Single

@Single
class GetAllUsersUseCase(
    private val userProvider: UserProvider,
) {
    operator fun invoke(): List<User> = userProvider.usersById().values.toList()
}
