package com.biwenger_client.infrastructure.network

sealed class Response<out T> {
    data class Success<T>(val body: T?) : Response<T>()
    data class Error(val code: Int, val message: String?) : Response<Nothing>()

    val isSuccessful: Boolean
        get() = this is Success
}
