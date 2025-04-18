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
import felix.livinglink.auth.LogoutRequest
import felix.livinglink.auth.LogoutResponse
import felix.livinglink.auth.network.AuthNetworkDataSource
import felix.livinglink.auth.network.AuthenticatedHttpClient
import felix.livinglink.auth.store.TokenStore
import felix.livinglink.common.model.LivingLinkResult
import felix.livinglink.defaultAppTestModule
import felix.livinglink.expectStates
import felix.livinglink.haptics.HapticsController
import felix.livinglink.ui.UiModule
import felix.livinglink.ui.common.navigation.Navigator
import felix.livinglink.ui.common.state.LoadableViewModelState
import felix.livinglink.ui.settings.SettingsViewModel
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class LogoutLogicTest {
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
    }

    @Test
    fun `test logout on success`() = runTest {
        // {"userId":"userId","username":"username"}
        val accessToken =
            "header.eyJ1c2VySWQiOiJ1c2VySWQiLCJ1c2VybmFtZSI6InVzZXJuYW1lIn0=.signature"

        val refreshToken = "refreshToken"

        // Arrange
        everySuspend {
            mockTokenStore.get()
        } returns BearerTokens(accessToken = accessToken, refreshToken = refreshToken)

        everySuspend {
            mockAuthNetworkDataSource.logout(any())
        } returns LivingLinkResult.Data(
            LogoutResponse.Success
        )

        appTestModule = defaultAppTestModule(
            navigator = mockNavigator,
            hapticsController = mockHapticsController,
            authNetworkDataSource = mockAuthNetworkDataSource,
            tokenStore = mockTokenStore,
        )

        val settingsViewModel = appTestModule.settingsViewModel

        turbineScope {
            val loading = settingsViewModel.loading.testIn(backgroundScope)
            val error = settingsViewModel.error.testIn(backgroundScope)
            val data = settingsViewModel.data.testIn(backgroundScope)
            val loadableData = settingsViewModel.loadableData.testIn(backgroundScope)

            // Act
            settingsViewModel.logout()

            // Assert
            assertFalse(loading.awaitItem())
            loading.ensureAllEventsConsumed()

            assertNull(error.awaitItem())
            error.ensureAllEventsConsumed()

            assertEquals(SettingsViewModel.Data(showDeleteUserAlert = false), data.awaitItem())
            data.ensureAllEventsConsumed()

            loadableData.expectStates(
                LoadableViewModelState.State.Loading(),
                LoadableViewModelState.State.Data(
                    AuthenticatedHttpClient.AuthSession.LoggedIn(
                        userId = "userId",
                        username = "username",
                    )
                ),
                LoadableViewModelState.State.Data(AuthenticatedHttpClient.AuthSession.LoggedOut),
            )
        }

        // Assert - side effects
        verifySuspend(exhaustiveOrder) {
            mockTokenStore.get()
            mockHapticsController.performLightImpact()
            mockAuthNetworkDataSource.logout(LogoutRequest(refreshToken = refreshToken))
            mockTokenStore.clear()
            mockHapticsController.performSuccess()
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