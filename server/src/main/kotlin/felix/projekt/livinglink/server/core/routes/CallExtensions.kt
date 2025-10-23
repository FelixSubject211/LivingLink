package felix.projekt.livinglink.server.core.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

val ApplicationCall.username: String
    get() {
        val principal = this.principal<JWTPrincipal>()
            ?: throw IllegalStateException("No JWTPrincipal found")
        return principal.payload.getClaim("preferred_username").asString()
            ?: throw IllegalStateException("Claim 'preferred_username' is missing in the token")
    }

val ApplicationCall.userId: String
    get() {
        val principal = this.principal<JWTPrincipal>()
            ?: throw IllegalStateException("No JWTPrincipal found")
        return principal.payload.getClaim("sub").asString()
            ?: throw IllegalStateException("Claim 'sub' is missing in the token")
    }