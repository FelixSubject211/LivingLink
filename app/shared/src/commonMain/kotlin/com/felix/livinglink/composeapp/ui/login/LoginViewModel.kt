package com.felix.livinglink.composeapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.auth.application.LoginUseCase
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginScreenState.empty)
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()

    fun onApiKeyChanged(apiKey: String) {
        _state.update {
            it.copy(
                apiKey = apiKey,
            )
        }
    }

    fun onSubmit() {
        val apiKey = _state.value.apiKey.trim()
        if (apiKey.isBlank()) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }

            val result = loginUseCase(apiKey)

            when(result) {
                is LoginResult.Success -> {
                    _state.update {
                        LoginScreenState.empty
                    }
                }
                is LoginResult.InvalidKey -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = LoginScreenState.Error.InvalidKey
                        )
                    }
                }
                is LoginResult.NetworkError -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = LoginScreenState.Error.NetworkError
                        )
                    }
                }
            }
        }
    }

    fun closeError() {
        viewModelScope.launch {
            _state.update {
                it.copy(error = null)
            }
        }
    }
}