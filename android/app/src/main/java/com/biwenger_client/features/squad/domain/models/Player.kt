package com.biwenger_client.features.squad.domain.models

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
