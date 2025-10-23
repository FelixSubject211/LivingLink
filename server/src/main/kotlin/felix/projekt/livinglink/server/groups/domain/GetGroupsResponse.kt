package felix.projekt.livinglink.server.groups.domain

sealed class GetGroupsResponse {
    data class Success(val groups: Map<String, Group>) : GetGroupsResponse()
    object NotModified : GetGroupsResponse()
}