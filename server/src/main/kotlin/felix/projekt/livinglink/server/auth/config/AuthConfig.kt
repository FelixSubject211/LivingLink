package felix.projekt.livinglink.server.auth.config

import io.github.cdimascio.dotenv.dotenv

interface AuthConfig {
    val keycloakHost: String
    val keycloakPort: Int
    val keycloakRealm: String
    val keycloakClientId: String
    val keycloakClientSecret: String
    val keycloakAdmin: String
    val keycloakAdminPassword: String
    val keycloakUserRole: String
    val authJwtName: String
}

fun authDefaultConfig(): AuthConfig {
    return object : AuthConfig {
        private val dotenv = dotenv()

        override val keycloakHost: String = dotenv["KEYCLOAK_HOST"]!!
        override val keycloakPort: Int = dotenv["KEYCLOAK_PORT"]!!.toInt()
        override val keycloakRealm: String = dotenv["KEYCLOAK_REALM"]!!
        override val keycloakClientId: String = dotenv["KEYCLOAK_CLIENT_ID"]!!
        override val keycloakClientSecret: String = dotenv["KEYCLOAK_CLIENT_SECRET"]!!
        override val keycloakAdmin: String = dotenv["KEYCLOAK_ADMIN"]!!
        override val keycloakAdminPassword: String = dotenv["KEYCLOAK_ADMIN_PASSWORD"]!!
        override val keycloakUserRole: String = dotenv["KEYCLOAK_USER_ROLE"]!!
        override val authJwtName: String = "auth-jwt"
    }
}