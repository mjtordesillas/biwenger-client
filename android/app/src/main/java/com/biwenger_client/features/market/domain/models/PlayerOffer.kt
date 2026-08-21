package com.biwenger_client.features.market.domain.models

import com.biwenger_client.domain.models.Player

// A standing offer on one of my squad players. Unlike MarketListing,
// there's no separate asking-price/market-value split here — `price` is
// just the catalogue's own value, same field the shared Player model
// carries, next to `amount` (what's being offered), `until` (when the
// offer expires, unix seconds — same shape as a listing's `until`), and
// `bidder` (who's offering, when identifiable — see biwenger-client's
// src/player-offer-view.js: `null` in every sample observed so far).
// Field names match that response shape.
data class PlayerOffer(
    val offerId: Long,
    val id: Int,
    val name: String,
    val position: Int,
    val secondaryPosition: Int?,
    val price: Long,
    val priceIncrement: Long,
    val points: Int,
    val photoUrl: String,
    val teamCrestUrl: String,
    val amount: Long,
    val until: Long,
    val bidder: String?,
) {
    // For reusing the shared player-detail sheet (price/performance
    // history, match-day drill-down) — same reasoning as
    // MarketListing.toPlayer().
    fun toPlayer() = Player(
        id = id,
        name = name,
        position = position,
        secondaryPosition = secondaryPosition,
        price = price,
        priceIncrement = priceIncrement,
        points = points,
        photoUrl = photoUrl,
        teamCrestUrl = teamCrestUrl,
    )
}
