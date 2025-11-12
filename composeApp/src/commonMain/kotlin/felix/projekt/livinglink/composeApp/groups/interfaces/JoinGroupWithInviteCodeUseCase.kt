package felix.projekt.livinglink.composeApp.groups.interfaces

interface JoinGroupWithInviteCodeUseCase {
    suspend operator fun invoke(inviteCodeKey: String): Response

    sealed class Response {
        data class Success(val group: Group) : Response()
        data object InvalidInviteCode : Response()
        data object AlreadyMember : Response()
        data object NetworkError : Response()
    }

    data class Group(
        val id: String,
        val name: String,
        val memberCount: Int
    )
}
