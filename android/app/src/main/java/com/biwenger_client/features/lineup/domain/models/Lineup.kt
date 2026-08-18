package com.biwenger_client.features.lineup.domain.models

import com.biwenger_client.domain.models.Player

// The starting eleven plus the active formation. `players` are plain
// Player views (biwenger-client's src/lineup-view.js reuses
// toPlayerView unchanged) — grouping them onto the pitch by row is done
// from each player's own `position` field, not by parsing `formation`
// or relying on list order (see docs/biwenger-api-notes.md § "Starting
// lineup" for why that's unnecessary).
data class Lineup(
    val formation: String,
    val players: List<Player>,
)
