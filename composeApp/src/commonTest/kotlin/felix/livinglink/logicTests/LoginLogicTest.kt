package felix.livinglink.logicTests

import app.cash.turbine.turbineScope
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import felix.livinglink.auth.LoginRequest
import felix.livinglink.auth.LoginResponse
import felix.livinglink.auth.network.AuthNetworkDataSource
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.auth.store.TokenStore
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.common.network.NetworkError
import felix.livinglink.defaultAppTestModule
import felix.livinglink.expectStates
import felix.livinglink.haptics.HapticsController
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.LoadableViewModelState
import felix.livinglink.ui.common.state.ViewModelState
import felix.livinglink.ui.login.LoginViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class LoginLogicTest {
    private lateinit var mockNavigator: Navigator
    private lateinit var mockHapticsController: HapticsController
    private lateinit var mockAuthNetworkDataSource: AuthNetworkDataSource
    private lateinit var mockTokenStore: TokenStore
    private lateinit var appTestModule: UiModule

    @BeforeTest
    fun setup() {
        mockNavigator = mock(mode = MockMode.autofill)
        mockHapticsController = mock(mode = MockMode.autofill)
        mockAuthNetworkDataSource = mock(mode = MockMode.autofill)
        mockTokenStore = mock(mode = MockMode.autofill)
        appTestModule = defaultAppTestModule(
            navigator = mockNavigator,
            hapticsController = mockHapticsController,
            authNetworkDataSource = mockAuthNetworkDataSource,
            tokenStore = mockTokenStore,
        )
    }

    @Test
    fun `test login on success`() = runTest {
        val username = "username"
        val password = "password"

        // {"userId":"userId","username":"username"}
        val accessToken =
            "header.eyJ1c2VySWQiOiJ1c2VySWQiLCJ1c2VybmFtZSI6InVzZXJuYW1lIn0=.signature"
        val refreshToken = "dummy-refresh-token"

        // Arrange
        everySuspend {
            mockAuthNetworkDataSource.login(any())
        } returns LivingLinkResult.Data(
            LoginResponse.Success(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        )

        val loginViewModel = appTestModule.loginViewModel()
        val settingsViewModel = appTestModule.settingsViewModel

        loginViewModel.updateUsername(username)
        loginViewModel.updatePassword(password)

        turbineScope {
            val loginViewModelLoading = loginViewModel.loading.testIn(backgroundScope)
            val loginViewModelError = loginViewModel.error.testIn(backgroundScope)
            val settingsViewModelLoadableData =
                settingsViewModel.loadableData.testIn(backgroundScope)

            // Act
            loginViewModel.login()

            // Assert
            assertFalse(loginViewModelLoading.awaitItem())
            loginViewModelLoading.ensureAllEventsConsumed()

            assertNull(loginViewModelError.awaitItem())
            loginViewModelError.ensureAllEventsConsumed()


            settingsViewModelLoadableData.expectStates(
                LoadableViewModelState.State.Loading(),
                LoadableViewModelState.State.Data(AuthenticatedHttpClient.AuthSession.LoggedOut),
                LoadableViewModelState.State.Data(
                    AuthenticatedHttpClient.AuthSession.LoggedIn(
                        userId = "userId",
                        username = "username",
                    )
                )
            )
        }

        // Assert - side effects
        verifySuspend(exhaustiveOrder) {
            mockTokenStore.get()
            mockHapticsController.performLightImpact()
            mockAuthNetworkDataSource.login(LoginRequest(username = username, password = password))
            mockTokenStore.set(accessToken = accessToken, refreshToken = refreshToken)
            mockNavigator.popAll()
            mockHapticsController.performSuccess()
        }

        verifyNoOtherCalls()
    }

    @Test
    fun `test login when network fails`() = runTest {
        // Arrange
        everySuspend {
            mockAuthNetworkDataSource.login(any())
        } returns LivingLinkResult.Error(NetworkError.IO)

        val loginViewModel = appTestModule.loginViewModel()

        turbineScope {
            val loading = loginViewModel.loading.testIn(backgroundScope)
            val error = loginViewModel.error.testIn(backgroundScope)

            // Act
            loginViewModel.login()

            // Assert
            assertFalse(loading.awaitItem())
            loading.ensureAllEventsConsumed()

            assertNull(error.awaitItem())
            assertEquals(ViewModelState.CombinedError.Request(NetworkError.IO), error.awaitItem())
            error.ensureAllEventsConsumed()
        }

        // Assert - side effects
        verifySuspend(exhaustiveOrder) {
            mockTokenStore.get()
            mockHapticsController.performLightImpact()
            mockAuthNetworkDataSource.login(any())
            mockHapticsController.performError()
        }

        verifyNoOtherCalls()
    }

    @Test
    fun `test login when credentials are invalid`() = runTest {
        val username = "username"
        val invalidPassword = "invalidPassword"

        // Arrange
        everySuspend {
            mockAuthNetworkDataSource.login(any())
        } returns LivingLinkResult.Data(LoginResponse.InvalidUsernameOrPassword)

        val loginViewModel = appTestModule.loginViewModel()

        loginViewModel.updateUsername(username)
        loginViewModel.updatePassword(invalidPassword)

        turbineScope {
            val loading = loginViewModel.loading.testIn(backgroundScope)
            val error = loginViewModel.error.testIn(backgroundScope)

            // Act
            loginViewModel.login()

            // Assert
            assertFalse(loading.awaitItem())
            loading.ensureAllEventsConsumed()

            assertNull(error.awaitItem())
            assertEquals(
                ViewModelState.CombinedError.Error(
                    LoginViewModel.Error.InvalidUsernameOrPassword
                ),
                error.awaitItem()
            )
            error.ensureAllEventsConsumed()
        }

        // Assert - side effects
        verifySuspend(exhaustiveOrder) {
            mockTokenStore.get()
            mockHapticsController.performLightImpact()
            mockAuthNetworkDataSource.login(
                LoginRequest(username = username, password = invalidPassword)
            )
            mockHapticsController.performError()
        }

        verifyNoOtherCalls()
    }

    private fun verifyNoOtherCalls() {
        verifyNoMoreCalls(
            mockNavigator,
            mockHapticsController,
            mockAuthNetworkDataSource,
            mockTokenStore
        )
    }
}