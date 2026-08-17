package com.biwenger_client.features.squad.infrastructure

import com.google.gson.reflect.TypeToken
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.infrastructure.network.HttpClient
import com.biwenger_client.infrastructure.network.Response
import com.biwenger_client.infrastructure.network.RetrofitHttpClient

class HttpMatchDayDetailsService(baseUrl: String, apiKey: String) : MatchDayDetailsService {
    private val httpClient: HttpClient = RetrofitHttpClient(baseUrl = baseUrl, apiKey = apiKey)

    override suspend fun matchDayDetails(playerId: Int, matchDay: Int, season: String): Response<MatchDayDetails> =
        httpClient.get(
            "players/$playerId/match-day-details?matchDay=$matchDay&season=$season",
            object : TypeToken<MatchDayDetails>() {}
        )
}
