package com.biwenger_client.features.lineup.domain.models

import com.biwenger_client.domain.models.Player

// The starting eleven plus the active formation. `players` are plain
// Player views (biwenger-client's src/lineup-view.js reuses
// toPlayerView unchanged), in Biwenger's own order — goalkeeper, then
// defenders/midfielders/forwards per `formation`'s counts — which
// `LineupScreen.kt`'s band-slicing relies on (see
// docs/biwenger-api-notes.md § "Starting lineup"). A `null` entry is a
// vacant slot at that index, not a missing/shorter list — see
// docs/biwenger-api-notes.md § "Starting lineup — write". `credits` is
// account-wide, not lineup-specific — it rides along because assigning
// a bench player via their secondary position costs it, silently (see
// the same doc section's credit-cost note); gates BenchCandidates'
// jollies.
data class Lineup(
    val formation: String,
    val players: List<Player?>,
    val credits: Int,
)
