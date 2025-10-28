package felix.projekt.livinglink.composeApp.groups.interfaces

import kotlinx.coroutines.flow.Flow

interface GetGroupUseCase {
    operator fun invoke(groupId: String): Flow<Response>

    sealed class Response {
        data object Loading : Response()
        data class Data(val group: Group) : Response()
    }

    data class Group(
        val id: String,
        val name: String
    )
}