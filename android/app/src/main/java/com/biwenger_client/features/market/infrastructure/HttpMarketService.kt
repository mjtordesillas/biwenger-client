package com.biwenger_client.features.market.infrastructure

import com.google.gson.reflect.TypeToken
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerOffer
import com.biwenger_client.infrastructure.network.HttpClient
import com.biwenger_client.infrastructure.network.Response
import com.biwenger_client.infrastructure.network.RetrofitHttpClient

// The backend wraps the list: { "players": [...] } — see
// biwenger-client's src/market-api-handler.js. Same wrapper shape as
// HttpSquadService's SquadResponseBody; not shared, since two of the
// same one-liner isn't a real duplication problem yet.
private data class MarketResponseBody(val players: List<MarketListing>)

// Same wrapper key ("players") as MarketResponseBody, different element
// type — see biwenger-client's src/player-offers-api-handler.js.
private data class PlayerOffersResponseBody(val players: List<PlayerOffer>)

class HttpMarketService(baseUrl: String, apiKey: String) : MarketService {
    private val httpClient: HttpClient = RetrofitHttpClient(baseUrl = baseUrl, apiKey = apiKey)

    override suspend fun market(): Response<List<MarketListing>> =
        when (val result = httpClient.get("market", object : TypeToken<MarketResponseBody>() {})) {
            is Response.Success -> Response.Success(result.body?.players ?: emptyList())
            is Response.Error -> result
        }

    override suspend fun myListings(): Response<List<MarketListing>> =
        when (val result = httpClient.get("market/my-listings", object : TypeToken<MarketResponseBody>() {})) {
            is Response.Success -> Response.Success(result.body?.players ?: emptyList())
            is Response.Error -> result
        }

    override suspend fun offers(): Response<List<PlayerOffer>> =
        when (val result = httpClient.get("market/offers", object : TypeToken<PlayerOffersResponseBody>() {})) {
            is Response.Success -> Response.Success(result.body?.players ?: emptyList())
            is Response.Error -> result
        }
}
