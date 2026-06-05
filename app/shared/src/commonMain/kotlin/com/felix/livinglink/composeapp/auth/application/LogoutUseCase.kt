package com.felix.livinglink.composeapp.auth.application

import com.felix.livinglink.composeapp.auth.domain.AuthRepository
import org.koin.core.annotation.Single

interface LogoutUseCase {
    suspend operator fun invoke()
}

@Single(binds = [LogoutUseCase::class])
class LogoutDefaultUseCase(
    private val authRepository: AuthRepository,
) : LogoutUseCase {
    override suspend fun invoke() {
        authRepository.clear()
    }
}