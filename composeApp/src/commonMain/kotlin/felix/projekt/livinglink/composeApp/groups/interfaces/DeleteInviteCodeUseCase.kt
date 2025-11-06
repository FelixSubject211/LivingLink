package felix.projekt.livinglink.composeApp.groups.interfaces

interface DeleteInviteCodeUseCase {
    suspend operator fun invoke(groupId: String, inviteCodeId: String): Response

    sealed class Response {
        data object Success : Response()
        data object NetworkError : Response()
    }
}