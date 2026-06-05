package com.felix.livinglink.composeapp.auth.application

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import com.felix.livinglink.composeapp.auth.domain.LoginResult
import org.koin.core.annotation.Single

interface LoginUseCase {
    suspend operator fun invoke(apiKey: String): LoginResult
}

@Single(binds = [LoginUseCase::class])
class LoginDefaultUseCase(
    private val authRepository: AuthRepository,
) : LoginUseCase {
    override suspend fun invoke(apiKey: String): LoginResult =
        authRepository.login(apiKey)
}