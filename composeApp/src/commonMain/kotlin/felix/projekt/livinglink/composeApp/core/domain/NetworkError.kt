package felix.projekt.livinglink.composeApp.core.domain

sealed class NetworkError : Error {
    data object IO : NetworkError()
    data object Unauthorized : NetworkError()
    data object ServerError : NetworkError()
}