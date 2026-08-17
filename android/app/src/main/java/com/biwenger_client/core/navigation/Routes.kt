package com.biwenger_client.core.navigation

// Ported from interest-tracker-android's Routes.kt, trimmed: no
// per-item sub-routes yet (squad/market player detail sheets are still
// in-screen conditional rendering, not navigation destinations) — add
// them here only once a screen actually needs a deep-linkable route.
object Routes {
    const val SQUAD = "squad"
    const val MARKET = "market"
}
