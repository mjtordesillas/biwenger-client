package com.biwenger_client.domain.models

// Promoted out of features/squad — market is a second feature that needs
// the same shape (biwenger-client's toPlayerView, reused unchanged by
// both /squad and /market). domain/ holds cross-feature domain models;
// see docs/coding-conventions/project-structure.md.
//
// position codes from Biwenger's catalogue: 1=GK 2=DF 3=MF 4=FW.
// See biwenger-client's docs/rat.md for where this comes from.
// Field names match biwenger-client's src/player-view.js response shape.
data class Player(
    val id: Int,
    val name: String,
    val position: Int,
    val secondaryPosition: Int?,
    val price: Long,
    val priceIncrement: Long,
    val points: Int,
    val photoUrl: String,
    val teamCrestUrl: String
)
