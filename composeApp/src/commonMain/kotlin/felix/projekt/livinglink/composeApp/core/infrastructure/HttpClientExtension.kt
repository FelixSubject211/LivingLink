package felix.projekt.livinglink.composeApp.core.infrastructure

import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import kotlinx.io.IOException

@OptIn(InternalAPI::class)
suspend inline fun <reified RESPONSE> HttpClient.get(
    urlString: String,
): Result<RESPONSE, NetworkError> {
    try {
        val response = this.get(urlString) {
            contentType(ContentType.Application.Json)
        }
        if (response.status == HttpStatusCode.Unauthorized) {
            return Result.Error(NetworkError.Unauthorized)
        }
        if (response.status == HttpStatusCode.InternalServerError) {
            return Result.Error(NetworkError.ServerError)
        }
        return Result.Success(response.body())
    } catch (e: IOException) {
        Napier.w("", e)
        return Result.Error(NetworkError.IO)
    }
}

@OptIn(InternalAPI::class)
suspend inline fun <reified REQUEST, reified RESPONSE> HttpClient.post(
    urlString: String,
    request: REQUEST,
): Result<RESPONSE, NetworkError> {
    try {
        val response = this.post(urlString) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status == HttpStatusCode.Unauthorized) {
            return Result.Error(NetworkError.Unauthorized)
        }
        if (response.status == HttpStatusCode.InternalServerError) {
            return Result.Error(NetworkError.ServerError)
        }
        return Result.Success(response.body())
    } catch (e: IOException) {
        Napier.w("", e)
        return Result.Error(NetworkError.IO)
    }
}

@OptIn(InternalAPI::class)
suspend inline fun <reified RESPONSE> HttpClient.delete(
    urlString: String
): Result<RESPONSE, NetworkError> {
    try {
        val response = this.delete(urlString) { }
        if (response.status == HttpStatusCode.Unauthorized) {
            return Result.Error(NetworkError.Unauthorized)
        }
        if (response.status == HttpStatusCode.InternalServerError) {
            return Result.Error(NetworkError.ServerError)
        }
        return Result.Success(response.body())
    } catch (e: IOException) {
        Napier.w("", e)
        return Result.Error(NetworkError.IO)
    }
}