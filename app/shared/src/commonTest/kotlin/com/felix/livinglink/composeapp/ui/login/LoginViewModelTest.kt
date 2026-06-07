package com.felix.livinglink.composeapp.ui.login

import com.felix.livinglink.composeapp.auth.application.LoginUseCase
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val loginSuccessResult = LoginResult.Success(
        apiKey = "apiKey",
        userId = "userId",
        username = "username",
    )

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        loginUseCase = mock()
        viewModel = LoginViewModel(loginUseCase = loginUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onApiKeyChanged updates state and enables submit`() {
        viewModel.onApiKeyChanged("my-key")

        val state = viewModel.state.value
        assertEquals("my-key", state.apiKey)
        assertTrue(state.canSubmit)
    }

    @Test
    fun `onApiKeyChanged disables submit for blank input`() {
        viewModel.onApiKeyChanged("   ")

        val state = viewModel.state.value
        assertFalse(state.canSubmit)
    }

    @Test
    fun `onSubmit calls use case with trimmed key`() = runTest {
        everySuspend { loginUseCase(any()) } returns loginSuccessResult

        viewModel.onApiKeyChanged("  my-key  ")
        viewModel.onSubmit()

        verifySuspend(VerifyMode.exactly(1)) {
            loginUseCase("my-key")
        }
    }

    @Test
    fun `onSubmit clears state on success`() = runTest {
        everySuspend { loginUseCase(any()) } returns loginSuccessResult

        viewModel.onApiKeyChanged("my-key")
        viewModel.onSubmit()

        val state = viewModel.state.value

        assertEquals("", state.apiKey)
        assertFalse(state.canSubmit)
        assertNull(state.error)
    }

    @Test
    fun `onSubmit sets invalid key error`() = runTest {
        everySuspend { loginUseCase(any()) } returns LoginResult.InvalidKey

        viewModel.onApiKeyChanged("bad-key")
        viewModel.onSubmit()

        val state = viewModel.state.value

        assertEquals(false, state.isLoading)
        assertEquals(LoginScreenState.Error.InvalidKey, state.error)
    }

    @Test
    fun `onSubmit sets network error`() = runTest {
        everySuspend { loginUseCase(any()) } returns LoginResult.NetworkError

        viewModel.onApiKeyChanged("key")
        viewModel.onSubmit()

        val state = viewModel.state.value

        assertEquals(false, state.isLoading)
        assertEquals(LoginScreenState.Error.NetworkError, state.error)
    }

    @Test
    fun `onSubmit does nothing when api key is blank`() = runTest {
        viewModel.onApiKeyChanged("   ")
        viewModel.onSubmit()

        verifySuspend(VerifyMode.exactly(0)) {
            loginUseCase(any())
        }
    }

    @Test
    fun `closeError clears error state`() = runTest {
        everySuspend { loginUseCase(any()) } returns LoginResult.InvalidKey

        viewModel.onApiKeyChanged("bad")
        viewModel.onSubmit()

        viewModel.closeError()

        assertNull(viewModel.state.value.error)
    }
}