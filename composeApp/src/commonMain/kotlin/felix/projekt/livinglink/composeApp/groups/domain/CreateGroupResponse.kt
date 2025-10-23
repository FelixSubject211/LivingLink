package felix.projekt.livinglink.composeApp.groups.domain

sealed class CreateGroupResponse {
    data class Success(val group: Group) : CreateGroupResponse()
}