package com.biwenger_client.features.market.domain.models

import com.biwenger_client.domain.models.Player

// A market listing is not a Player — squad ownership has one price, a
// listing has an asking price *and* a market value, plus a seller and an
// expiry Player has no concept of. Kept feature-local (unlike Player,
// which squad and market share unchanged) rather than bolting these
// fields onto Player as nullables. Field names match biwenger-client's
// src/market-listing-view.js response shape.
data class MarketListing(
    val id: Int,
    val name: String,
    val position: Int,
    val secondaryPosition: Int?,
    val price: Long,
    val marketValue: Long,
    val priceIncrement: Long,
    val points: Int,
    val photoUrl: String,
    val teamCrestUrl: String,
    val until: Long,
    val seller: String?,
) {
    // For reusing the shared player-detail sheet (price/performance
    // history, match-day drill-down) — that sheet only needs Player's
    // fields, and shows *market value*, not this listing's asking price
    // (the detail sheet is about the player, not any specific listing).
    fun toPlayer() = Player(
        id = id,
        name = name,
        position = position,
        secondaryPosition = secondaryPosition,
        price = marketValue,
        priceIncrement = priceIncrement,
        points = points,
        photoUrl = photoUrl,
        teamCrestUrl = teamCrestUrl,
    )
}
