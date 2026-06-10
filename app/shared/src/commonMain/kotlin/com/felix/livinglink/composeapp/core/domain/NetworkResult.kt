package com.felix.livinglink.composeapp.core.domain

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>

    data object Unauthorized : NetworkResult<Nothing>

    data object NetworkError : NetworkResult<Nothing>

    fun <T2>map(transform: (T) -> T2): NetworkResult<T2> {
       return when(this) {
           is Success -> Success(value = transform(this.value))
           is Unauthorized -> Unauthorized
           is NetworkError -> NetworkError
        }
    }
}