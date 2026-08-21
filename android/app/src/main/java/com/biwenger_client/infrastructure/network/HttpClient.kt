package com.biwenger_client.infrastructure.network

import com.google.gson.reflect.TypeToken

interface HttpClient {
    suspend fun <T> get(url: String, typeToken: TypeToken<T>): Response<T>
    suspend fun <T> put(url: String, body: Any, typeToken: TypeToken<T>): Response<T>
    suspend fun <T> delete(url: String, typeToken: TypeToken<T>): Response<T>
    // No-body POST — every POST our own backend takes so far needs
    // nothing from the client (e.g. listPlayer's price is fixed
    // server-side); add a body variant if a future POST needs one.
    suspend fun <T> post(url: String, typeToken: TypeToken<T>): Response<T>
}
