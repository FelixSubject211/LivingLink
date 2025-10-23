package felix.projekt.livinglink.composeApp.auth.application

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.domain.LoginResponse
import felix.projekt.livinglink.composeApp.auth.domain.TokenResponse
import felix.projekt.livinglink.composeApp.auth.interfaces.LoginUserUseCase
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class LoginUserDefaultUseCaseTest {
    private lateinit var mockAuthTokenManager: AuthTokenManager
    private lateinit var mockAuthNetworkDataSource: AuthNetworkDataSource
    private lateinit var sut: LoginUserDefaultUseCase

    @BeforeTest
    fun setup() {
        mockAuthTokenManager = mock(mode = MockMode.autofill)
        mockAuthNetworkDataSource = mock(mode = MockMode.autofill)
        sut = LoginUserDefaultUseCase(
            authTokenManager = mockAuthTokenManager,
            authNetworkDataSource = mockAuthNetworkDataSource
        )
    }

    @Test
    fun `Successful login - stores tokens and returns success`() = runTest {
        val username = "username"
        val password = "password"
        val accessToken = "accessToken"
        val refreshToken = "refreshToken"
        val expiresIn = 100L

        everySuspend {
            mockAuthNetworkDataSource.login(username = username, password = password)
        } returns Result.Success(
            LoginResponse.Success(
                TokenResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn
                )
            )
        )

        val result = sut.invoke(username = username, password = password)

        assertIs<LoginUserUseCase.Response.Success>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthNetworkDataSource.login(username = username, password = password)
            mockAuthTokenManager.setTokens(accessToken, refreshToken)
        }
        verifyNoMoreCalls(mockAuthTokenManager, mockAuthNetworkDataSource)
    }

    @Test
    fun `InvalidCredentials login - not store tokens and returns InvalidCredentials`() = runTest {
        val username = "username"
        val password = "password"

        everySuspend {
            mockAuthNetworkDataSource.login(username = username, password = password)
        } returns Result.Success(
            LoginResponse.InvalidCredentials
        )

        val result = sut.invoke(username = username, password = password)

        assertIs<LoginUserUseCase.Response.InvalidCredentials>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthNetworkDataSource.login(username = username, password = password)
        }
        verifyNoMoreCalls(mockAuthTokenManager, mockAuthNetworkDataSource)
    }

    @Test
    fun `Failed login - not store tokens and returns error`() = runTest {
        val username = "username"
        val password = "wrongPassword"
        val networkError: NetworkError = NetworkError.ServerError

        everySuspend {
            mockAuthNetworkDataSource.login(username = username, password = password)
        } returns Result.Error(error = networkError)

        val result = sut.invoke(username = username, password = password)

        assertIs<LoginUserUseCase.Response.NetworkError>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthNetworkDataSource.login(username = username, password = password)
        }
        verifyNoMoreCalls(mockAuthTokenManager, mockAuthNetworkDataSource)
    }
}
