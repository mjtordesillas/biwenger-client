package com.biwenger_client.features.squad.infrastructure

import com.google.gson.reflect.TypeToken
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.infrastructure.network.HttpClient
import com.biwenger_client.infrastructure.network.Response
import com.biwenger_client.infrastructure.network.RetrofitHttpClient

class HttpPriceHistoryService(baseUrl: String, apiKey: String) : PriceHistoryService {
    private val httpClient: HttpClient = RetrofitHttpClient(baseUrl = baseUrl, apiKey = apiKey)

    override suspend fun priceHistory(playerId: Int): Response<PriceHistory> =
        httpClient.get("players/$playerId/price-history", object : TypeToken<PriceHistory>() {})
}
