package com.felix.livinglink.server.user.config

import com.felix.livinglink.server.core.config.Env
import com.felix.livinglink.server.core.domain.User
import org.koin.core.annotation.Single

@Single
class StdioUserSettings {
    val user: User by lazy {
        User(
            id = Env.required("LIVINGLINK_STDIO_USER_ID"),
            username = Env.required("LIVINGLINK_STDIO_USERNAME"),
        )
    }
}
