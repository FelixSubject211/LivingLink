package felix.livinglink.common.network

import felix.livinglink.common.model.LivingLinkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.io.IOException

suspend inline fun <reified REQUEST, reified RESPONSE> HttpClient.post(
    urlString: String,
    request: REQUEST,
): LivingLinkResult<RESPONSE, NetworkError> {
    try {
        val response = this.post(urlString) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status == HttpStatusCode.NotFound) {
            return LivingLinkResult.Error(NetworkError.NotFound)
        }
        return LivingLinkResult.Data(response.body())
    } catch (e: IOException) {
        return LivingLinkResult.Error(NetworkError.IO)
    } catch (e: Throwable) {
        return LivingLinkResult.Error(NetworkError.Unknown(error = e))
    }
}

suspend inline fun <reified REQUEST, reified RESPONSE> HttpClient.delete(
    urlString: String,
    request: REQUEST,
): LivingLinkResult<RESPONSE, NetworkError> {
    try {
        val response = this.delete(urlString) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status == HttpStatusCode.NotFound) {
            return LivingLinkResult.Error(NetworkError.NotFound)
        }
        return LivingLinkResult.Data(response.body())
    } catch (e: IOException) {
        return LivingLinkResult.Error(NetworkError.IO)
    } catch (e: Throwable) {
        return LivingLinkResult.Error(NetworkError.Unknown(error = e))
    }
}