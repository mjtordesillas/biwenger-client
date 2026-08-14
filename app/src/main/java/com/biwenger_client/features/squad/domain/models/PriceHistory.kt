package com.biwenger_client.features.squad.domain.models

// Field names match biwenger-client's src/price-history-view.js response
// shape: the full trailing ~1 year window ("Last Year"), plus where the
// current season starts within it ("Current season") — the client slices
// `prices` by `seasonStart` rather than the backend pre-filtering, since
// the sheet shows both views.
data class PricePoint(
    val date: String,
    val price: Long
)

data class PriceHistory(
    val seasonStart: String,
    val prices: List<PricePoint>
)
