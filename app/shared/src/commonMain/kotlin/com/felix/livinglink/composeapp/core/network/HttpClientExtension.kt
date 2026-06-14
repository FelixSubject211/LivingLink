package com.felix.livinglink.composeapp.core.network

import com.felix.livinglink.composeapp.core.domain.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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

suspend inline fun <reified Body, reified T> HttpClient.postNetworkResult(
    urlString: String,
    body: Body,
    block: HttpRequestBuilder.() -> Unit = {}
): NetworkResult<T> {
    try {
        val response: HttpResponse =
            post(urlString) {
                contentType(ContentType.Application.Json)
                setBody(body)
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