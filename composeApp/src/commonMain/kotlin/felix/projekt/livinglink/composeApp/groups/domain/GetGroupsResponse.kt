package felix.projekt.livinglink.composeApp.groups.domain

sealed class GetGroupsResponse() {
    data class Success(
        val groups: Map<String, Group>,
        val nextPollAfterMillis: Long
    ) : GetGroupsResponse()

    data class NotModified(val nextPollAfterMillis: Long) : GetGroupsResponse()
}