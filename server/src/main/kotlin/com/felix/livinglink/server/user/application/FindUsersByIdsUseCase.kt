package com.felix.livinglink.server.user.application

import com.felix.livinglink.server.core.domain.User
import com.felix.livinglink.server.user.domain.UserProvider
import org.koin.core.annotation.Single

@Single
class FindUsersByIdsUseCase(
    private val userProvider: UserProvider,
) {
    operator fun invoke(ids: Iterable<String>): Map<String, User> {
        val idsSet = ids.toSet()
        return userProvider
            .usersById()
            .filter { idsSet.contains(it.key) }
    }
}
