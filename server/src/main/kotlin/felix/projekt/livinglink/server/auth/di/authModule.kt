package felix.projekt.livinglink.server.auth.di

import felix.projekt.livinglink.server.auth.application.DeleteUserDefaultUseCase
import felix.projekt.livinglink.server.auth.application.LoginUserDefaultUseCase
import felix.projekt.livinglink.server.auth.application.LogoutUserDefaultUseCase
import felix.projekt.livinglink.server.auth.application.RefreshUserTokenDefaultUseCase
import felix.projekt.livinglink.server.auth.application.RegisterUserDefaultUseCase
import felix.projekt.livinglink.server.auth.config.AuthConfig
import felix.projekt.livinglink.server.auth.config.authDefaultConfig
import felix.projekt.livinglink.server.auth.domain.AuthClient
import felix.projekt.livinglink.server.auth.infrastructure.KeycloakClient
import felix.projekt.livinglink.server.auth.interfaces.DeleteUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.LoginUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.LogoutUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.RefreshUserTokenUseCase
import felix.projekt.livinglink.server.auth.interfaces.RegisterUserUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    single<AuthConfig> { authDefaultConfig() }

    single<AuthClient> {
        KeycloakClient(
            authConfig = get()
        )
    }

    factoryOf(::LoginUserDefaultUseCase) bind LoginUserUseCase::class
    factoryOf(::RegisterUserDefaultUseCase) bind RegisterUserUseCase::class
    factoryOf(::RefreshUserTokenDefaultUseCase) bind RefreshUserTokenUseCase::class
    factoryOf(::LogoutUserDefaultUseCase) bind LogoutUserUseCase::class
    factoryOf(::DeleteUserDefaultUseCase) bind DeleteUserUseCase::class
}