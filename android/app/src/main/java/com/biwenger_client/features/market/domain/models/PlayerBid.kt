package com.biwenger_client.features.market.domain.models

import com.biwenger_client.domain.models.Player

// One of my own outgoing bids on another manager's player. Same shape as
// MarketListing (asking price, catalogue market value, seller, expiry)
// plus `amount` — the bid itself. Kept feature-local and separate from
// MarketListing (rather than bolting `amount` on as a nullable) for the
// same reason MarketListing and PlayerOffer are separate — each response
// shape stands for a different fact about a player. Field names match
// biwenger-client's src/player-bid-view.js response shape.
data class PlayerBid(
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
    val amount: Long,
) {
    // For reusing the shared player-detail sheet — same reasoning as
    // MarketListing.toPlayer().
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
