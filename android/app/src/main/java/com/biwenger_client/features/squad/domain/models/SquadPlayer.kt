package com.biwenger_client.features.squad.domain.models

import com.biwenger_client.domain.models.Player

// A squad player is not a plain Player — squad ownership carries facts
// that matter for squad-management decisions ("can/should I sell this
// one") a market listing or a bare catalogue player doesn't have:
// `lockedUntil` (Biwenger's post-purchase transfer lock — unix seconds
// for when it lifts, or null if already sellable), `inMarket` (I've
// currently listed this player), `hasOffer` (someone has a standing
// offer on it), and `status` (fitness — "ok"/"injured"/"doubt"/...).
// Kept feature-local rather than bolting these onto the shared model,
// same reasoning as MarketListing. Field names match biwenger-client's
// src/squad-player-view.js response shape.
data class SquadPlayer(
    val id: Int,
    val name: String,
    val position: Int,
    val secondaryPosition: Int?,
    val price: Long,
    val priceIncrement: Long,
    val points: Int,
    val photoUrl: String,
    val teamCrestUrl: String,
    val lockedUntil: Long?,
    val inMarket: Boolean,
    val hasOffer: Boolean,
    val status: String,
) {
    // For reusing the shared player-detail sheet (price/performance
    // history, match-day drill-down) — that sheet only needs Player's
    // fields, not this squad player's ownership/market/fitness facts.
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
