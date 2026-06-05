package com.felix.livinglink.server.user.domain

import com.felix.livinglink.server.core.domain.User

interface UserProvider {
    fun usersById(): Map<String, User>
}
