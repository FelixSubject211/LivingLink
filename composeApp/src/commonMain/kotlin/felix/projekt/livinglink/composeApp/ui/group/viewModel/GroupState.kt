package felix.projekt.livinglink.composeApp.ui.group.viewModel

import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class GroupState(
    val groupId: String? = null,
    val groupName: String? = null,
    val memberIdToMemberName: Map<String, String> = emptyMap(),
    val inviteCodes: List<InviteCode> = emptyList(),
    val groupIsLoading: Boolean = false,
    val inviteCodeCreation: InviteCodeCreationState = InviteCodeCreationState.None,
    val showDeleteInviteCodeConfirmation: Boolean = false,
    val inviteCodeIdToDeleted: String? = null,
    val deleteInviteCodeIsLoading: Boolean = false
) {
    data class InviteCode(
        val id: String,
        val name: String,
        val creatorId: String,
        val usages: Int
    )

    sealed class InviteCodeCreationState {
        object None : InviteCodeCreationState()
        data class Input(
            val name: String,
            val isLoading: Boolean,
            val error: Error?
        ) : InviteCodeCreationState() {
            sealed class Error {
                data object NetworkError : Error()
            }
        }

        data class Success(val key: String) : InviteCodeCreationState()
    }
}
