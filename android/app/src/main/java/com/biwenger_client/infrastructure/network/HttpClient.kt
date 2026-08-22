package com.biwenger_client.infrastructure.network

import com.google.gson.reflect.TypeToken

interface HttpClient {
    suspend fun <T> get(url: String, typeToken: TypeToken<T>): Response<T>
    suspend fun <T> put(url: String, body: Any, typeToken: TypeToken<T>): Response<T>
    suspend fun <T> delete(url: String, typeToken: TypeToken<T>): Response<T>
    // No-body POST — every POST our own backend took until placeBid
    // needed one (listPlayer's price, cycleListings' selection, etc.
    // are all fixed/decided server-side).
    suspend fun <T> post(url: String, typeToken: TypeToken<T>): Response<T>
    // Body variant, for a POST that does need one — see placeBid.
    suspend fun <T> post(url: String, body: Any, typeToken: TypeToken<T>): Response<T>
}
