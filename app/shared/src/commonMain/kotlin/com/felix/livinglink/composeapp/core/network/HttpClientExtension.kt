package com.felix.livinglink.composeapp.core.network

import com.felix.livinglink.composeapp.core.domain.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

suspend inline fun <reified T> HttpClient.getNetworkResult(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): NetworkResult<T> {
    try {
        val response: HttpResponse =
            get(urlString) {
                block()
            }

        return when {
            response.status.isSuccess() -> {
                NetworkResult.Success(
                    value = response.body()
                )
            }

            response.status == HttpStatusCode.Unauthorized ->
                NetworkResult.Unauthorized

            else ->
                NetworkResult.NetworkError
        }
    } catch (_: Exception) {
        return NetworkResult.NetworkError
    }
}