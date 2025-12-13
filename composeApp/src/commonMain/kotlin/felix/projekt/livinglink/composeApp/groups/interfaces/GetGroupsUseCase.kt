package felix.projekt.livinglink.composeApp.groups.interfaces

import kotlinx.coroutines.flow.Flow

interface GetGroupsUseCase {
    operator fun invoke(): Flow<Response>

    sealed class Response {
        data object Loading : Response()
        data class Data(val groups: List<Group>) : Response()
    }

    data class Group(
        val id: String,
        val name: String,
        val memberCount: Int
    )
}