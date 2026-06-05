package com.felix.livinglink.composeapp.ui.login

data class LoginScreenState(
    val apiKey: String,
    val isLoading: Boolean,
    val error: Error?
) {
    val canSubmit : Boolean
        get() = apiKey.isNotBlank() && !isLoading

    sealed class Error {
        data object InvalidKey: Error()
        data object NetworkError: Error()
    }

    companion object {
        val empty = LoginScreenState(
            apiKey = "",
            isLoading = false,
            error = null
        )
    }
}