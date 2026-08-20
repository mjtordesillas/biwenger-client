package com.biwenger_client.features.lineup.domain.models

// Free formations only, in Biwenger's own picker order — confirmed
// against Biwenger's own UI (2026-08-20), not the API (which exposes no
// formations list at all — checked). See docs/biwenger-api-notes.md §
// "Starting lineup — write" and
// docs/backlog/in-progress/change-lineup-formation.md: anything beyond
// this list is a paid "extra" formation, deliberately excluded from the
// picker entirely rather than offered-but-costly.
val FreeFormations = listOf("3-4-3", "3-5-2", "4-3-3", "4-4-2", "4-5-1", "5-3-2", "5-4-1")
