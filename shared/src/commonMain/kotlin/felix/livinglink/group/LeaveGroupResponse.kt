package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
sealed class LeaveGroupResponse {
    @Serializable
    data object Success : LeaveGroupResponse()

    @Serializable
    data object NotAllowed : LeaveGroupResponse()

    @Serializable
    data object Error : LeaveGroupResponse()

    @Serializable
    data object LastAdminCannotLeave : LeaveGroupResponse()
}
