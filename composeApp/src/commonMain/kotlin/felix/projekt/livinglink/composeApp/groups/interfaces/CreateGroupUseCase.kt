package felix.projekt.livinglink.composeApp.groups.interfaces

interface CreateGroupUseCase {
    suspend operator fun invoke(groupName: String): Response

    sealed class Response {
        data object Success : Response()
        data object NetworkError : Response()
    }
}