package com.biwenger_client.features.lineup.infrastructure

import com.google.gson.reflect.TypeToken
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.infrastructure.network.HttpClient
import com.biwenger_client.infrastructure.network.Response
import com.biwenger_client.infrastructure.network.RetrofitHttpClient

// Unlike squad/market, the backend's body already *is* the shape we
// need — { "formation": ..., "players": [...] } — see biwenger-client's
// src/lineup-api-handler.js. No wrapper data class to unwrap.
class HttpLineupService(baseUrl: String, apiKey: String) : LineupService {
    private val httpClient: HttpClient = RetrofitHttpClient(baseUrl = baseUrl, apiKey = apiKey)

    override suspend fun lineup(): Response<Lineup> =
        httpClient.get("lineup", object : TypeToken<Lineup>() {})

    override suspend fun saveLineup(formation: String, playerIds: List<Int?>): Response<Lineup> =
        httpClient.put("lineup", SaveLineupRequest(formation = formation, playerIds = playerIds), object : TypeToken<Lineup>() {})
}
