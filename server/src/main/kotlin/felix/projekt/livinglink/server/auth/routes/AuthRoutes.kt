package felix.projekt.livinglink.server.auth.routes

import felix.projekt.livinglink.server.auth.config.AuthConfig
import felix.projekt.livinglink.server.auth.domain.LoginResponse
import felix.projekt.livinglink.server.auth.domain.RefreshResponse
import felix.projekt.livinglink.server.auth.domain.RegisterResponse
import felix.projekt.livinglink.server.auth.domain.TokenResponse
import felix.projekt.livinglink.server.auth.interfaces.DeleteUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.LoginUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.LogoutUserUseCase
import felix.projekt.livinglink.server.auth.interfaces.RefreshUserTokenUseCase
import felix.projekt.livinglink.server.auth.interfaces.RegisterUserUseCase
import felix.projekt.livinglink.server.core.routes.userId
import felix.projekt.livinglink.server.core.routes.username
import felix.projekt.livinglink.shared.auth.requestModel.AuthRequest
import felix.projekt.livinglink.shared.auth.requestModel.AuthResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(
    authConfig: AuthConfig,
    loginUserUseCase: LoginUserUseCase,
    registerUserUseCase: RegisterUserUseCase,
    refreshUserTokenUseCase: RefreshUserTokenUseCase,
    logoutUserUseCase: LogoutUserUseCase,
    deleteUserUseCase: DeleteUserUseCase
) {
    route("/auth") {
        fun TokenResponse.toRequestModel() = AuthResponse.TokenResponse(
            accessToken = this.accessToken,
            refreshToken = this.refreshToken,
            expiresIn = this.expiresIn
        )

        post("/login") {
            val request: AuthRequest.Login = call.receive()
            val response: LoginResponse = loginUserUseCase(
                username = request.username,
                password = request.password
            )
            when (response) {
                is LoginResponse.Success -> {
                    call.respond<AuthResponse.Login>(
                        AuthResponse.Login.Success(
                            tokenResponse = response.tokenResponse.toRequestModel()
                        )
                    )
                }

                is LoginResponse.InvalidCredentials -> {
                    call.respond<AuthResponse.Login>(AuthResponse.Login.InvalidCredentials)
                }
            }
        }

        post("/refresh") {
            val request: AuthRequest.Refresh = call.receive()
            val response: RefreshResponse = refreshUserTokenUseCase(
                refreshToken = request.refreshToken
            )
            when (response) {
                is RefreshResponse.Success -> {
                    call.respond<AuthResponse.Refresh>(
                        AuthResponse.Refresh.Success(
                            tokenResponse = response.tokenResponse.toRequestModel()
                        )
                    )
                }

                is RefreshResponse.TokenExpired -> {
                    call.respond<AuthResponse.Refresh>(AuthResponse.Refresh.TokenExpired)
                }
            }
        }

        post("/register") {
            val request: AuthRequest.Register = call.receive()
            val response: RegisterResponse = registerUserUseCase(
                username = request.username,
                password = request.password
            )
            when (response) {
                is RegisterResponse.Success -> {
                    call.respond<AuthResponse.Register>(
                        AuthResponse.Register.Success(
                            tokenResponse = response.tokenResponse.toRequestModel()
                        )
                    )
                }

                is RegisterResponse.PolicyViolation -> {
                    call.respond<AuthResponse.Register>(AuthResponse.Register.PolicyViolation)
                }

                is RegisterResponse.UserAlreadyExists -> {
                    call.respond<AuthResponse.Register>(AuthResponse.Register.UserAlreadyExists)
                }
            }
        }

        post("/logout") {
            val request: AuthRequest.Logout = call.receive()
            logoutUserUseCase(refreshToken = request.refreshToken)
            call.respond<AuthResponse.Logout>(AuthResponse.Logout.Success)
        }

        authenticate(authConfig.authJwtName) {
            delete("/user") {
                deleteUserUseCase(userId = call.userId, username = call.username)
                call.respond<AuthResponse.DeleteUser>(AuthResponse.DeleteUser.Success)
            }
        }
    }
}