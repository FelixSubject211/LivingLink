package felix.projekt.livinglink.server.auth.infrastructure

import felix.projekt.livinglink.server.auth.config.AuthConfig
import felix.projekt.livinglink.server.auth.domain.AuthClient
import felix.projekt.livinglink.server.auth.domain.LoginResponse
import felix.projekt.livinglink.server.auth.domain.RefreshResponse
import felix.projekt.livinglink.server.auth.domain.RegisterResponse
import felix.projekt.livinglink.server.auth.domain.TokenResponse
import felix.projekt.livinglink.server.core.domain.AuthIntegrationException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class KeycloakClient(
    private val authConfig: AuthConfig,
    private val client: HttpClient = HttpClient { install(ContentNegotiation) { json() } }
) : AuthClient {
    private val baseUrl = "http://${authConfig.keycloakHost}:${authConfig.keycloakPort}"

    private suspend fun requestToken(params: Parameters): TokenResponse? {
        try {
            val responseText = client.submitForm(
                url = "$baseUrl/realms/${authConfig.keycloakRealm}/protocol/openid-connect/token",
                formParameters = params,
                encodeInQuery = false
            ) {
                method = HttpMethod.Companion.Post
            }.bodyAsText()

            val json = Json.Default.parseToJsonElement(responseText).jsonObject
            val accessToken = json["access_token"]?.jsonPrimitive?.content
            val refreshToken = json["refresh_token"]?.jsonPrimitive?.content
            val expiresIn = json["expires_in"]?.jsonPrimitive?.longOrNull ?: 0L

            return if (accessToken != null && refreshToken != null) {
                TokenResponse(accessToken, refreshToken, expiresIn)
            } else {
                null
            }
        } catch (ex: ClientRequestException) {
            val status = ex.response.status.value
            val body = ex.response.bodyAsText()
            return when {
                status == 400 && body.contains("invalid_grant") -> null
                status in listOf(400, 401) -> null
                else -> throw AuthIntegrationException("HTTP $status: $body")
            }
        }
    }

    override suspend fun login(username: String, password: String): LoginResponse {
        return try {
            val tokenResponse = requestToken(
                Parameters.Companion.build {
                    append("grant_type", "password")
                    append("client_id", authConfig.keycloakClientId)
                    append("client_secret", authConfig.keycloakClientSecret)
                    append("username", username)
                    append("password", password)
                }
            )
            tokenResponse?.let { LoginResponse.Success(it) } ?: LoginResponse.InvalidCredentials
        } catch (ex: Exception) {
            throw AuthIntegrationException(ex.message ?: "Unexpected error")
        }
    }

    override suspend fun refresh(refreshToken: String): RefreshResponse {
        return try {
            val tokenResponse = requestToken(
                Parameters.Companion.build {
                    append("grant_type", "refresh_token")
                    append("client_id", authConfig.keycloakClientId)
                    append("client_secret", authConfig.keycloakClientSecret)
                    append("refresh_token", refreshToken)
                }
            )
            tokenResponse?.let { RefreshResponse.Success(it) } ?: RefreshResponse.TokenExpired
        } catch (ex: Exception) {
            throw AuthIntegrationException(ex.message ?: "Unexpected error")
        }
    }

    private suspend fun getAdminToken(): String {
        val response = client.submitForm(
            url = "$baseUrl/realms/master/protocol/openid-connect/token",
            formParameters = Parameters.Companion.build {
                append("grant_type", "password")
                append("client_id", "admin-cli")
                append("username", authConfig.keycloakAdmin)
                append("password", authConfig.keycloakAdminPassword)
            },
            encodeInQuery = false
        ) { method = HttpMethod.Companion.Post }

        val json = Json.Default.parseToJsonElement(response.bodyAsText()).jsonObject
        return json["access_token"]?.jsonPrimitive?.content
            ?: throw AuthIntegrationException("Admin token not found")
    }

    override suspend fun register(username: String, password: String): RegisterResponse {
        try {
            val adminToken = getAdminToken()

            val userJson = buildJsonObject {
                put("username", username)
                put("enabled", true)
                putJsonArray("credentials") {
                    add(buildJsonObject {
                        put("type", "password")
                        put("value", password)
                        put("temporary", false)
                    })
                }
            }

            val createResponse = client.post("$baseUrl/admin/realms/${authConfig.keycloakRealm}/users") {
                header("Authorization", "Bearer $adminToken")
                contentType(ContentType.Application.Json)
                setBody(userJson)
            }

            when (createResponse.status.value) {
                201 -> {}
                409 -> return RegisterResponse.UserAlreadyExists
                400 -> return RegisterResponse.PolicyViolation
                else -> throw AuthIntegrationException("Keycloak returned status ${createResponse.status.value}")
            }

            val usersListText = client.get("$baseUrl/admin/realms/${authConfig.keycloakRealm}/users") {
                header("Authorization", "Bearer $adminToken")
                parameter("username", username)
            }.bodyAsText()
            val usersList = Json.Default.parseToJsonElement(usersListText).jsonArray
            val userId = usersList.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
                ?: throw AuthIntegrationException("User not found after creation")

            val roleText =
                client.get("$baseUrl/admin/realms/${authConfig.keycloakRealm}/roles/${authConfig.keycloakUserRole}") {
                    header("Authorization", "Bearer $adminToken")
                }.bodyAsText()
            val roleRep = Json.Default.parseToJsonElement(roleText).jsonObject

            client.post("$baseUrl/admin/realms/${authConfig.keycloakRealm}/users/$userId/role-mappings/realm") {
                header("Authorization", "Bearer $adminToken")
                contentType(ContentType.Application.Json)
                setBody(Json.Default.encodeToString(JsonArray.Companion.serializer(), JsonArray(listOf(roleRep))))
            }

            val loginResponse = login(username = username, password = password)
            when (loginResponse) {
                is LoginResponse.Success -> {
                    return RegisterResponse.Success(tokenResponse = loginResponse.tokenResponse)
                }

                LoginResponse.InvalidCredentials -> {
                    throw AuthIntegrationException("User cant login after Register")
                }
            }
        } catch (ex: Exception) {
            throw AuthIntegrationException(ex.message ?: "Unexpected error")
        }
    }

    override suspend fun logout(refreshToken: String) {
        try {
            client.submitForm(
                url = "$baseUrl/realms/${authConfig.keycloakRealm}/protocol/openid-connect/logout",
                formParameters = Parameters.Companion.build {
                    append("client_id", authConfig.keycloakClientId)
                    append("client_secret", authConfig.keycloakClientSecret)
                    append("refresh_token", refreshToken)
                },
                encodeInQuery = false
            ) { method = HttpMethod.Companion.Post }.bodyAsText()
        } catch (ex: ClientRequestException) {
            throw AuthIntegrationException("HTTP ${ex.response.status.value}")
        } catch (ex: Exception) {
            throw AuthIntegrationException(ex.message ?: "Unexpected error")
        }
    }

    override suspend fun deleteUser(username: String) {
        try {
            val adminToken = getAdminToken()

            val usersListText = client.get("$baseUrl/admin/realms/${authConfig.keycloakRealm}/users") {
                header("Authorization", "Bearer $adminToken")
                parameter("username", username)
            }.bodyAsText()
            val usersList = Json.Default.parseToJsonElement(usersListText).jsonArray
            val userId = usersList.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
                ?: throw AuthIntegrationException("User not found")

            val deleteResponse =
                client.request("$baseUrl/admin/realms/${authConfig.keycloakRealm}/users/$userId") {
                    method = HttpMethod.Delete
                    header("Authorization", "Bearer $adminToken")
                }

            return when (deleteResponse.status.value) {
                204 -> {}
                404 -> throw AuthIntegrationException("User not found in Keycloak")
                else -> throw AuthIntegrationException("Keycloak returned status ${deleteResponse.status.value}")
            }
        } catch (ex: Exception) {
            throw AuthIntegrationException(ex.message ?: "Unexpected error")
        }
    }
}