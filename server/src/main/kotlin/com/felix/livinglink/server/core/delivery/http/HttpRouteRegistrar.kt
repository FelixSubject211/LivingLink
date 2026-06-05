package com.felix.livinglink.server.core.delivery.http

import io.ktor.server.routing.Route

fun interface HttpRouteRegistrar {
    fun register(route: Route)
}
