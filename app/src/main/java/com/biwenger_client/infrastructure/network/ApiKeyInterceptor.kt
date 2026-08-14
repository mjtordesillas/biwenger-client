package com.biwenger_client.infrastructure.network

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(API_KEY_HEADER, apiKey)
            .build()
        return chain.proceed(request)
    }

    companion object {
        const val API_KEY_HEADER = "x-api-key"
    }
}
