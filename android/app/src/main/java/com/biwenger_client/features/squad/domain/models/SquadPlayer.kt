package com.biwenger_client.features.squad.domain.models

import com.biwenger_client.domain.models.Player

// A squad player is not a plain Player — squad ownership carries facts
// that matter for squad-management decisions ("can/should I sell this
// one") a market listing or a bare catalogue player doesn't have:
// `signedAt` (unix seconds for when this ownership started — always
// present), `signedPrice` (what was paid, or null for a draft-owned
// player who was never bought), `draftedPrice` (that player's market
// value on `signedAt` — only meaningful when `signedPrice` is null),
// `lockedUntil` (Biwenger's post-purchase transfer lock — unix seconds
// for when it lifts, or null if already sellable), `inMarket` (I've
// currently listed this player), `offerAmount` (a standing offer's
// amount, or null — the raw number rather than a boolean, so the UI can
// judge it against `price`, this view's market value), and `status`
// (fitness — "ok"/"injured"/"doubt"/...). Kept feature-local rather than
// bolting these onto the shared model, same reasoning as MarketListing.
// Field names match biwenger-client's src/squad-player-view.js response
// shape.
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
    val signedAt: Long,
    val signedPrice: Long?,
    val draftedPrice: Long?,
    val lockedUntil: Long?,
    val inMarket: Boolean,
    val offerAmount: Long?,
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

    // The squad list's ascending sort order: goalkeepers, then each
    // outfield position banded by secondary role — the role shared with
    // the adjacent lower position first, no secondary in the middle,
    // the role shared with the adjacent higher position last (skipped
    // where that neighbor doesn't apply, e.g. defenders have no lower
    // neighbor to share a secondary with). A combination the request
    // didn't cover (e.g. a defender with a forward secondary) sorts
    // last — not expected in practice; `altPositions` has only ever
    // been observed with a single, adjacent entry.
    val positionSortRank: Int
        get() = when {
            position == 1 -> 0
            position == 2 && secondaryPosition == null -> 1
            position == 2 && secondaryPosition == 3 -> 2
            position == 3 && secondaryPosition == 2 -> 3
            position == 3 && secondaryPosition == null -> 4
            position == 3 && secondaryPosition == 4 -> 5
            position == 4 && secondaryPosition == 3 -> 6
            position == 4 && secondaryPosition == null -> 7
            else -> 8
        }
}
