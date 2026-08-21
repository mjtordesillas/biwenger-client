package com.biwenger_client.features.market.infrastructure

import com.google.gson.reflect.TypeToken
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerBid
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

// Same wrapper key, different element type again — see biwenger-client's
// src/player-bids-api-handler.js.
private data class PlayerBidsResponseBody(val players: List<PlayerBid>)
private data class RejectOfferRequest(val status: String = "rejected")
private data class RejectOfferResponseBody(val status: Int)
// Same request/response shape as reject, on a separate endpoint path —
// see backend's accept-player-offer-api-handler.js.
private data class AcceptOfferRequest(val status: String = "accepted")
private data class AcceptOfferResponseBody(val status: Int)

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

    override suspend fun bids(): Response<List<PlayerBid>> =
        when (val result = httpClient.get("market/my-bids", object : TypeToken<PlayerBidsResponseBody>() {})) {
            is Response.Success -> Response.Success(result.body?.players ?: emptyList())
            is Response.Error -> result
        }

    override suspend fun rejectOffer(offerId: Long): Response<Unit> =
        when (httpClient.put("market/offers/$offerId", RejectOfferRequest(), object : TypeToken<RejectOfferResponseBody>() {})) {
            is Response.Success -> Response.Success(Unit)
            is Response.Error -> Response.Error(502, "upstream_error")
        }

    override suspend fun acceptOffer(offerId: Long): Response<Unit> =
        when (httpClient.put("market/offers/$offerId/accept", AcceptOfferRequest(), object : TypeToken<AcceptOfferResponseBody>() {})) {
            is Response.Success -> Response.Success(Unit)
            is Response.Error -> Response.Error(502, "upstream_error")
        }
}
