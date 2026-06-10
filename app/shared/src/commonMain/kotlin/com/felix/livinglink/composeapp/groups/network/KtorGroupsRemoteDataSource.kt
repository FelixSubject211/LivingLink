package com.felix.livinglink.composeapp.groups.network

import com.felix.livinglink.composeapp.config.BuildKonfig
import com.felix.livinglink.composeapp.core.domain.NetworkResult
import com.felix.livinglink.composeapp.core.network.getNetworkResult
import com.felix.livinglink.composeapp.groups.domain.Group
import com.felix.livinglink.composeapp.groups.domain.GroupsRemoteDataSource
import com.felix.livinglink.shared.groups.GetGroupsForUserRequestV1
import com.felix.livinglink.shared.groups.GetGroupsForUserResponseV1
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import org.koin.core.annotation.Single
import kotlin.collections.List

@Single(binds = [GroupsRemoteDataSource::class])
class KtorGroupsRemoteDataSource(
    private val httpClient: HttpClient,
) : GroupsRemoteDataSource {
    override suspend fun getGroups(apiKey: String): NetworkResult<List<Group>> =
        httpClient.getNetworkResult<GetGroupsForUserResponseV1>("${BuildKonfig.BASE_URL}${GetGroupsForUserRequestV1.ROUTE}") {
            bearerAuth(apiKey)
        }.map { response -> response.groups.map { Group(id = it.id, name = it.name) } }
    }