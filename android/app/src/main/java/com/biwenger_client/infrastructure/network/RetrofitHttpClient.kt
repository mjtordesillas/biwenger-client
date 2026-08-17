package com.biwenger_client.infrastructure.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response as RetrofitResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.lang.reflect.Type

class RetrofitHttpClient(
    private val baseUrl: String,
    private val apiKey: String,
    private val gson: Gson = Gson()
) : HttpClient {
    private val retrofitClient: RetrofitClient = buildRetrofitClient(baseUrl, apiKey, gson).create(RetrofitClient::class.java)

    override suspend fun <T> get(url: String, typeToken: TypeToken<T>): Response<T> {
        val response = retrofitClient.get(baseUrl + url)
        return convertResponse(response, typeToken.type)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> convertResponse(response: RetrofitResponse<ResponseBody>, type: Type): Response<T> {
        if (!response.isSuccessful) {
            return Response.Error(response.code(), response.message())
        }
        val jsonString = response.body()?.string()
        if (jsonString.isNullOrEmpty()) {
            return Response.Success(null)
        }
        val convertedBody = gson.fromJson(jsonString, type) as T
        return Response.Success(convertedBody)
    }

    private interface RetrofitClient {
        @GET
        suspend fun get(@Url url: String): RetrofitResponse<ResponseBody>
    }

    companion object {
        private fun buildRetrofitClient(baseUrl: String, apiKey: String, gson: Gson): Retrofit {
            val securedClient = OkHttpClient.Builder()
                .addInterceptor(ApiKeyInterceptor(apiKey = apiKey))
                .build()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(securedClient)
                .build()
        }
    }
}
