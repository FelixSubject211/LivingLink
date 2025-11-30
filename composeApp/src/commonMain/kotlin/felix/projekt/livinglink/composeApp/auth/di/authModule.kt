package felix.projekt.livinglink.composeApp.auth.di

import felix.projekt.livinglink.composeApp.auth.application.DeleteUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.GetAuthSessionDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.GetAuthStateDefaultService
import felix.projekt.livinglink.composeApp.auth.application.LoginUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.LogoutUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.application.RegisterUserDefaultUseCase
import felix.projekt.livinglink.composeApp.auth.domain.AuthNetworkDataSource
import felix.projekt.livinglink.composeApp.auth.domain.AuthTokenManager
import felix.projekt.livinglink.composeApp.auth.domain.TokenStore
import felix.projekt.livinglink.composeApp.auth.infrastructure.AuthNetworkDefaultDataSource
import felix.projekt.livinglink.composeApp.auth.infrastructure.AuthTokenDefaultManager
import felix.projekt.livinglink.composeApp.auth.infrastructure.getTokenPlatformStore
import felix.projekt.livinglink.composeApp.auth.interfaces.DeleteUserUseCase
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthSessionUseCase
import felix.projekt.livinglink.composeApp.auth.interfaces.GetAuthStateService
import felix.projekt.livinglink.composeApp.auth.interfaces.LoginUserUseCase
import felix.projekt.livinglink.composeApp.auth.interfaces.LogoutUserUseCase
import felix.projekt.livinglink.composeApp.auth.interfaces.RegisterUserUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single<TokenStore> { getTokenPlatformStore() }

    single<AuthNetworkDataSource> {
        AuthNetworkDefaultDataSource(
            httpClient = get()
        )
    }

    single<AuthTokenManager> {
        AuthTokenDefaultManager(
            authNetworkDataSource = get(),
            tokenStore = get(),
            scope = get()
        )
    }

    factoryOf(::GetAuthStateDefaultService) bind GetAuthStateService::class
    factoryOf(::LoginUserDefaultUseCase) bind LoginUserUseCase::class
    factoryOf(::RegisterUserDefaultUseCase) bind RegisterUserUseCase::class
    factoryOf(::LogoutUserDefaultUseCase) bind LogoutUserUseCase::class
    factoryOf(::DeleteUserDefaultUseCase) bind DeleteUserUseCase::class
    factoryOf(::GetAuthSessionDefaultUseCase) bind GetAuthSessionUseCase::class
}