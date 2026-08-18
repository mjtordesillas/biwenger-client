package com.biwenger_client.features.squad.infrastructure

import com.google.gson.reflect.TypeToken
import com.biwenger_client.features.squad.domain.models.SquadPlayer
import com.biwenger_client.infrastructure.network.HttpClient
import com.biwenger_client.infrastructure.network.Response
import com.biwenger_client.infrastructure.network.RetrofitHttpClient

// The backend wraps the list: { "players": [...] } — see
// biwenger-client's src/squad-api-handler.js.
private data class SquadResponseBody(val players: List<SquadPlayer>)

class HttpSquadService(baseUrl: String, apiKey: String) : SquadService {
    private val httpClient: HttpClient = RetrofitHttpClient(baseUrl = baseUrl, apiKey = apiKey)

    override suspend fun squad(): Response<List<SquadPlayer>> =
        when (val result = httpClient.get("squad", object : TypeToken<SquadResponseBody>() {})) {
            is Response.Success -> Response.Success(result.body?.players ?: emptyList())
            is Response.Error -> result
        }
}
