package felix.livinglink.common.network

import NetworkErrorLocalizables
import felix.livinglink.common.model.LivingLinkError

sealed class NetworkError : LivingLinkError {
    data object IO : NetworkError() {
        override fun title() = NetworkErrorLocalizables.ioErrorTitle()
    }

    data object NotFound : NetworkError() {
        override fun title() = NetworkErrorLocalizables.notFoundTitle()
        override fun message() = NetworkErrorLocalizables.notFoundMessage()
    }

    data object Unauthorized : NetworkError() {
        override fun title() = NetworkErrorLocalizables.unauthorizedErrorTitle()
    }

    data class Unknown(val error: Throwable) : NetworkError() {
        override fun title() = NetworkErrorLocalizables.unknownErrorTitle()
        override fun message() = error.message
    }
}