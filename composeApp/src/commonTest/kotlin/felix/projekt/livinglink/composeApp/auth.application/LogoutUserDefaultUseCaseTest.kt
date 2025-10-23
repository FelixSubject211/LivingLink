package felix.projekt.livinglink.composeApp.auth.application

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthSession
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.interfaces.LogoutUserUseCase
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class LogoutUserDefaultUseCaseTest {
    private lateinit var mockAuthTokenManager: AuthTokenManager
    private lateinit var mockAuthNetworkDataSource: AuthNetworkDataSource
    private lateinit var sessionFlow: MutableStateFlow<AuthSession>
    private lateinit var sut: LogoutUserDefaultUseCase

    @BeforeTest
    fun setup() {
        mockAuthTokenManager = mock(mode = MockMode.autofill)
        mockAuthNetworkDataSource = mock(mode = MockMode.autofill)
        sessionFlow = MutableStateFlow(AuthSession.LoggedOut)
        every { mockAuthTokenManager.session } returns sessionFlow
        sut = LogoutUserDefaultUseCase(
            authTokenManager = mockAuthTokenManager,
            authNetworkDataSource = mockAuthNetworkDataSource
        )
    }

    @Test
    fun `Successful logout - clear tokens and returns success`() = runTest {
        val userId = "userId"
        val username = "username"
        val refreshToken = "refreshToken"
        sessionFlow.value = AuthSession.LoggedIn(
            userId = userId,
            username = username,
            refreshToken = refreshToken
        )

        everySuspend {
            mockAuthNetworkDataSource.logout(refreshToken = refreshToken)
        } returns Result.Success(Unit)

        val result = sut.invoke()

        assertIs<LogoutUserUseCase.Response.Success>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthTokenManager.session
            mockAuthNetworkDataSource.logout(refreshToken = refreshToken)
            mockAuthTokenManager.clearTokens()
        }
        verifyNoMoreCalls(mockAuthNetworkDataSource, mockAuthTokenManager)
    }

    @Test
    fun `Unauthorized logout - clear tokens and returns Unauthorized `() = runTest {
        val userId = "userId"
        val username = "username"
        val refreshToken = "refreshToken"
        sessionFlow.value = AuthSession.LoggedIn(
            userId = userId,
            username = username,
            refreshToken = refreshToken
        )

        everySuspend {
            mockAuthNetworkDataSource.logout(refreshToken = refreshToken)
        } returns Result.Error(NetworkError.Unauthorized)

        val result = sut.invoke()

        assertIs<LogoutUserUseCase.Response.Unauthorized>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthTokenManager.session
            mockAuthNetworkDataSource.logout(refreshToken = refreshToken)
            mockAuthTokenManager.clearTokens()
        }
        verifyNoMoreCalls(mockAuthNetworkDataSource, mockAuthTokenManager)
    }

    @Test
    fun `Failed logout - clear tokens and returns error`() = runTest {
        val userId = "userId"
        val username = "username"
        val refreshToken = "refreshToken"
        sessionFlow.value = AuthSession.LoggedIn(
            userId = userId,
            username = username,
            refreshToken = refreshToken
        )

        everySuspend {
            mockAuthNetworkDataSource.logout(refreshToken = refreshToken)
        } returns Result.Error(NetworkError.IO)

        val result = sut.invoke()

        assertIs<LogoutUserUseCase.Response.NetworkError>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthTokenManager.session
            mockAuthNetworkDataSource.logout(refreshToken = refreshToken)
            mockAuthTokenManager.clearTokens()
        }
        verifyNoMoreCalls(mockAuthNetworkDataSource, mockAuthTokenManager)
    }
}