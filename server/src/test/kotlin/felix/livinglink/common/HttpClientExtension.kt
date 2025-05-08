package felix.livinglink.common

import felix.livinglink.auth.LoginResponse
import felix.livinglink.auth.RegisterRequest
import felix.livinglink.auth.RegisterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.serializer
import kotlin.test.assertIs

suspend inline fun <reified RESPONSE> HttpClient.get(
    urlString: String,
    token: String? = null,
): RESPONSE {
    val response = get(urlString) {
        contentType(ContentType.Application.Json)
        token?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
        }
    }
    assertEquals(HttpStatusCode.OK, response.status)
    return json.decodeFromString<RESPONSE>(response.body<String>())
}

suspend inline fun <reified REQUEST, reified RESPONSE> HttpClient.post(
    urlString: String,
    request: REQUEST,
    token: String? = null,
): RESPONSE {
    val response = post(urlString) {
        contentType(ContentType.Application.Json)
        setBody(json.encodeToString(json.serializersModule.serializer(), request))
        token?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
        }
    }
    return json.decodeFromString<RESPONSE>(response.body<String>())
}

suspend inline fun <reified RESPONSE> HttpClient.delete(
    urlString: String,
    token: String? = null,
): RESPONSE {
    val response = delete(urlString) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        token?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
        }
    }
    return json.decodeFromString(response.body())
}

suspend inline fun HttpClient.registerUser(
    username: String,
    password: String
): RegisterResponse.Success {
    val response: RegisterResponse = post(
        urlString = "auth/register",
        request = RegisterRequest(username = username, password = password)
    )
    assertIs<RegisterResponse.Success>(response)
    return response
}

suspend inline fun HttpClient.loginUser(
    username: String,
    password: String
): LoginResponse.Success {
    val response: LoginResponse = post(
        urlString = "auth/login",
        request = RegisterRequest(username = username, password = password)
    )
    assertIs<LoginResponse.Success>(response)
    return response
}