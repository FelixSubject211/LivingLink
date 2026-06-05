package com.felix.livinglink.composeapp.auth.network

import com.felix.livinglink.composeapp.auth.domain.AuthRemoteDataSource
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import com.felix.livinglink.composeapp.config.BuildKonfig
import com.felix.livinglink.shared.login.LoginRequest
import com.felix.livinglink.shared.login.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Single

@Single(binds = [AuthRemoteDataSource::class])
class KtorAuthRemoteDataSource(
    private val httpClient: HttpClient,
) : AuthRemoteDataSource {
    override suspend fun login(apiKey: String): LoginResult =
        try {
            val response: LoginResponse =
                httpClient
                    .post("${BuildKonfig.BASE_URL}${LoginRequest.ROUTE}") {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequest(apiKey = apiKey))
                    }.body()

            when (response) {
                is LoginResponse.Success -> LoginResult.Success
                is LoginResponse.InvalidKey -> LoginResult.InvalidKey
            }
        } catch (_: Exception) {
            LoginResult.NetworkError
        }
}