package com.biwenger_client.infrastructure.network

import com.google.gson.reflect.TypeToken

interface HttpClient {
    suspend fun <T> get(url: String, typeToken: TypeToken<T>): Response<T>
    suspend fun <T> put(url: String, body: Any, typeToken: TypeToken<T>): Response<T>
    suspend fun <T> delete(url: String, typeToken: TypeToken<T>): Response<T>
}
