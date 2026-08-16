package com.biwenger_client.features.squad.infrastructure

import com.google.gson.reflect.TypeToken
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.infrastructure.network.HttpClient
import com.biwenger_client.infrastructure.network.Response
import com.biwenger_client.infrastructure.network.RetrofitHttpClient

class HttpPerformanceHistoryService(baseUrl: String, apiKey: String) : PerformanceHistoryService {
    private val httpClient: HttpClient = RetrofitHttpClient(baseUrl = baseUrl, apiKey = apiKey)

    override suspend fun performanceHistory(playerId: Int, season: String): Response<PerformanceHistory> =
        httpClient.get("players/$playerId/performance-history?season=$season", object : TypeToken<PerformanceHistory>() {})
}
