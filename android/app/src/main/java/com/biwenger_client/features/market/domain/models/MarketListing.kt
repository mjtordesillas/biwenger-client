package com.biwenger_client.features.market.domain.models

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
)
