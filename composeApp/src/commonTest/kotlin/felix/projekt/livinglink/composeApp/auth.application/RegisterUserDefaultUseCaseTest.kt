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
import felix.projekt.livinglink.composeApp.auth.domain.RegisterResponse
import felix.projekt.livinglink.composeApp.auth.domain.TokenResponse
import felix.projekt.livinglink.composeApp.auth.interfaces.RegisterUserUseCase
import felix.projekt.livinglink.composeApp.core.domain.NetworkError
import felix.projekt.livinglink.composeApp.core.domain.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class RegisterUserDefaultUseCaseTest {
    private lateinit var mockAuthTokenManager: AuthTokenManager
    private lateinit var mockAuthNetworkDataSource: AuthNetworkDataSource
    private lateinit var sut: RegisterUserDefaultUseCase

    @BeforeTest
    fun setup() {
        mockAuthTokenManager = mock(mode = MockMode.autofill)
        mockAuthNetworkDataSource = mock(mode = MockMode.autofill)
        sut = RegisterUserDefaultUseCase(
            authTokenManager = mockAuthTokenManager,
            authNetworkDataSource = mockAuthNetworkDataSource
        )
    }

    @Test
    fun `Successful register - stores tokens and returns success`() = runTest {
        val username = "newUser"
        val password = "password"
        val accessToken = "accessToken"
        val refreshToken = "refreshToken"
        val expiresIn = 100L

        everySuspend {
            mockAuthNetworkDataSource.register(username = username, password = password)
        } returns Result.Success(
            RegisterResponse.Success(
                TokenResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn
                )
            )
        )

        val result = sut.invoke(username = username, password = password)

        assertIs<RegisterUserUseCase.Response.Success>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthNetworkDataSource.register(username = username, password = password)
            mockAuthTokenManager.setTokens(accessToken, refreshToken)
        }
        verifyNoMoreCalls(mockAuthNetworkDataSource, mockAuthTokenManager)
    }

    @Test
    fun `UserAlreadyExists register - stores tokens and returns UserAlreadyExists`() = runTest {
        val username = "newUser"
        val password = "password"

        everySuspend {
            mockAuthNetworkDataSource.register(username = username, password = password)
        } returns Result.Success(
            RegisterResponse.UserAlreadyExists
        )

        val result = sut.invoke(username = username, password = password)

        assertIs<RegisterUserUseCase.Response.UserAlreadyExists>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthNetworkDataSource.register(username = username, password = password)
        }
        verifyNoMoreCalls(mockAuthNetworkDataSource, mockAuthTokenManager)
    }

    @Test
    fun `PolicyViolation register - stores tokens and returns PolicyViolation`() = runTest {
        val username = "newUser"
        val password = "password"

        everySuspend {
            mockAuthNetworkDataSource.register(username = username, password = password)
        } returns Result.Success(
            RegisterResponse.PolicyViolation
        )

        val result = sut.invoke(username = username, password = password)

        assertIs<RegisterUserUseCase.Response.PolicyViolation>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthNetworkDataSource.register(username = username, password = password)
        }
        verifyNoMoreCalls(mockAuthNetworkDataSource, mockAuthTokenManager)
    }

    @Test
    fun `Failed register - not store tokens and returns error`() = runTest {
        val username = "newUser"
        val password = "password"
        val error = NetworkError.IO

        everySuspend {
            mockAuthNetworkDataSource.register(username = username, password = password)
        } returns Result.Error(error)

        val result = sut.invoke(username = username, password = password)

        assertIs<RegisterUserUseCase.Response.NetworkError>(result)

        verifySuspend(exhaustiveOrder) {
            mockAuthNetworkDataSource.register(username = username, password = password)
        }
        verifyNoMoreCalls(mockAuthNetworkDataSource, mockAuthTokenManager)
    }
}