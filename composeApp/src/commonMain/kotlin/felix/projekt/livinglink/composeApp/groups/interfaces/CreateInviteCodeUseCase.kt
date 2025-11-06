package felix.projekt.livinglink.composeApp.groups.interfaces

interface CreateInviteCodeUseCase {
    suspend operator fun invoke(groupId: String, inviteCodeName: String): Response

    sealed class Response {
        data class Success(val key: String) : Response()
        data object NetworkError : Response()
    }
}