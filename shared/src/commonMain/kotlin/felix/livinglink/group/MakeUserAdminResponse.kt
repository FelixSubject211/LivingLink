package felix.livinglink.group

import kotlinx.serialization.Serializable

@Serializable
sealed class MakeUserAdminResponse {
    @Serializable
    data object Success : MakeUserAdminResponse()

    @Serializable
    data object NotAllowed : MakeUserAdminResponse()

    @Serializable
    data object Error : MakeUserAdminResponse()
}
