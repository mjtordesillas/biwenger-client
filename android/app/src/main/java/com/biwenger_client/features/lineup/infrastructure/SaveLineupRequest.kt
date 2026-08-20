package com.biwenger_client.features.lineup.infrastructure

// The PUT /lineup request body — field names match what
// biwenger-client's src/save-lineup-api-handler.js reads off
// event.body. `playerIds` mirrors Lineup.players' order, ids only,
// `null` at a slot left vacant (see docs/biwenger-api-notes.md §
// "Starting lineup — write").
data class SaveLineupRequest(
    val formation: String,
    val playerIds: List<Int?>,
)
