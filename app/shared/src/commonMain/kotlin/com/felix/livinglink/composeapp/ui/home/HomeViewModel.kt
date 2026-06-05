package com.felix.livinglink.composeapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.livinglink.composeapp.auth.application.LogoutUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}