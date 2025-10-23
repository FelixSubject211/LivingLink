package felix.projekt.livinglink.shared.groups.requestModel

import kotlinx.serialization.Serializable

@Serializable
sealed class GroupRequest {
    @Serializable
    data class GetGroups(val currentGroupVersions: Map<String, Long>) : GroupRequest()

    @Serializable
    data class CreateGroup(val groupName: String) : GroupRequest()
}