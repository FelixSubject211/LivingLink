package com.felix.livinglink.composeapp.auth.network

import com.felix.livinglink.composeapp.auth.domain.AuthRemoteDataSource
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import com.felix.livinglink.composeapp.config.BuildKonfig
import com.felix.livinglink.shared.auth.LoginRequestV1
import com.felix.livinglink.shared.auth.LoginResponseV1
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
            val response: LoginResponseV1 =
                httpClient
                    .post("${BuildKonfig.BASE_URL}${LoginRequestV1.ROUTE}") {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequestV1(apiKey = apiKey))
                    }.body()

            when (response) {
                is LoginResponseV1.Success -> LoginResult.Success(
                    apiKey = apiKey,
                    userId = response.userId,
                    username = response.username,
                )
                is LoginResponseV1.InvalidKey -> LoginResult.InvalidKey
            }
        } catch (_: Exception) {
            LoginResult.NetworkError
        }
}