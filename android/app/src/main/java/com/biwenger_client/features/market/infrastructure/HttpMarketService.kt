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
// DELETE has no request body — see backend's unlist-player-api-handler.js.
private data class UnlistPlayerResponseBody(val status: Int)
// POST has no request body either — the fixed listing price is applied
// server-side, see backend's list-player-api-handler.js.
private data class ListPlayerResponseBody(val status: Int)
// No request body — the whole selection happens server-side now, see
// backend's cycle-listings-api-handler.js. The response carries which
// ids were unlisted/listed, but the client doesn't need it (the my-
// listings reload after CYCLE_LISTINGS_FINISHED_EVENT is what actually
// updates the UI), so it's parsed but discarded into Unit like the
// other writes.
private data class CycleListingsResponseBody(val unlisted: List<Int> = emptyList(), val listed: List<Int> = emptyList())
// DELETE has no request body, same as unlist — see backend's
// remove-bid-api-handler.js. The real upstream call returns 204 (see
// docs/biwenger-api-notes.md § "My outgoing bids — write (remove)"),
// but the backend's own private write proxy always echoes `{}`/200,
// same as every other write here — nothing to parse either way.
private data class RemoveBidResponseBody(val status: Int)
// Request/response shape for placing a bid — see backend's
// place-bid-api-handler.js. Unlike the other writes, this POST does
// carry a body: the amount is user-entered, not fixed server-side.
private data class PlaceBidRequest(val amount: Long)
private data class PlaceBidResponseBody(val status: Int)

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

    override suspend fun unlistPlayer(playerId: Int): Response<Unit> =
        when (httpClient.delete("market/my-listings/$playerId", object : TypeToken<UnlistPlayerResponseBody>() {})) {
            is Response.Success -> Response.Success(Unit)
            is Response.Error -> Response.Error(502, "upstream_error")
        }

    override suspend fun listPlayer(playerId: Int): Response<Unit> =
        when (httpClient.post("market/my-listings/$playerId", object : TypeToken<ListPlayerResponseBody>() {})) {
            is Response.Success -> Response.Success(Unit)
            is Response.Error -> Response.Error(502, "upstream_error")
        }

    override suspend fun cycleListings(): Response<Unit> =
        when (httpClient.post("market/cycle-listings", object : TypeToken<CycleListingsResponseBody>() {})) {
            is Response.Success -> Response.Success(Unit)
            is Response.Error -> Response.Error(502, "upstream_error")
        }

    override suspend fun removeBid(offerId: Long): Response<Unit> =
        when (httpClient.delete("market/my-bids/$offerId", object : TypeToken<RemoveBidResponseBody>() {})) {
            is Response.Success -> Response.Success(Unit)
            is Response.Error -> Response.Error(502, "upstream_error")
        }

    override suspend fun placeBid(playerId: Int, amount: Long): Response<Unit> =
        when (
            httpClient.post(
                "market/my-bids/$playerId",
                PlaceBidRequest(amount = amount),
                object : TypeToken<PlaceBidResponseBody>() {}
            )
        ) {
            is Response.Success -> Response.Success(Unit)
            is Response.Error -> Response.Error(502, "upstream_error")
        }
}
