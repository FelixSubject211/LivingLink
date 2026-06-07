package com.felix.livinglink.server.core.delivery.http

import com.felix.livinglink.server.core.domain.User
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext

fun RoutingContext.requireUser(): User =
    requireNotNull(call.principal<UserPrincipal>()) {
        "No authenticated user in call."
    }.user
