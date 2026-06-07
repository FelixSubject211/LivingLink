package com.felix.livinglink.composeapp.groups.network

import com.felix.livinglink.composeapp.config.BuildKonfig
import com.felix.livinglink.composeapp.groups.domain.GetGroupsResult
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsRemoteDataSource
import com.felix.livinglink.shared.groups.GetGroupsForUserRequestV1
import com.felix.livinglink.shared.groups.GetGroupsForUserResponseV1
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import org.koin.core.annotation.Single

@Single(binds = [GroupsRemoteDataSource::class])
class KtorGroupsRemoteDataSource(
    private val httpClient: HttpClient,
) : GroupsRemoteDataSource {
    override suspend fun getGroups(apiKey: String): GetGroupsResult =
        try {
            val response: HttpResponse =
                httpClient.get("${BuildKonfig.BASE_URL}${GetGroupsForUserRequestV1.ROUTE}") {
                    bearerAuth(apiKey)
                }

            when {
                response.status.isSuccess() -> {
                    val body: GetGroupsForUserResponseV1 = response.body()
                    GetGroupsResult.Success(
                        groups = body.groups.map { Group(id = it.id, name = it.name) },
                    )
                }

                response.status == HttpStatusCode.Unauthorized ->
                    GetGroupsResult.Unauthorized

                else ->
                    GetGroupsResult.NetworkError
            }
        } catch (_: Exception) {
            GetGroupsResult.NetworkError
        }
}