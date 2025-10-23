package felix.projekt.livinglink.server.auth.interfaces

import felix.projekt.livinglink.server.auth.domain.RefreshResponse

interface RefreshUserTokenUseCase {
    suspend operator fun invoke(refreshToken: String): RefreshResponse
}