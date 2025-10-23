package felix.projekt.livinglink.server.auth.application

import felix.projekt.livinglink.server.auth.domain.AuthClient
import felix.projekt.livinglink.server.auth.domain.RefreshResponse
import felix.projekt.livinglink.server.auth.interfaces.RefreshUserTokenUseCase

class RefreshUserTokenDefaultUseCase(
    private val authClient: AuthClient
) : RefreshUserTokenUseCase {
    override suspend fun invoke(refreshToken: String): RefreshResponse {
        return authClient.refresh(refreshToken)
    }
}