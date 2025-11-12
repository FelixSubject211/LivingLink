package felix.projekt.livinglink.composeApp.groups.domain

sealed class JoinGroupResponse {
    data class Success(val group: Group) : JoinGroupResponse()
    data object InviteCodeNotFound : JoinGroupResponse()
    data object AlreadyMember : JoinGroupResponse()
}
