package com.biwenger_client.helpers.builders

import com.biwenger_client.features.squad.domain.models.SquadPlayer

fun aSquadPlayer(
    id: Int = 1,
    name: String = "Brugué",
    position: Int = 4,
    secondaryPosition: Int? = null,
    price: Long = 280000,
    priceIncrement: Long = 0,
    points: Int = 0,
    photoUrl: String = "https://cdn.biwenger.com/i/p/$id.png",
    teamCrestUrl: String = "https://cdn.biwenger.com/i/t/1.png",
    lockedUntil: Long? = null,
    inMarket: Boolean = false,
    offerAmount: Long? = null,
    status: String = "ok",
) = SquadPlayer(
    id = id,
    name = name,
    position = position,
    secondaryPosition = secondaryPosition,
    price = price,
    priceIncrement = priceIncrement,
    points = points,
    photoUrl = photoUrl,
    teamCrestUrl = teamCrestUrl,
    lockedUntil = lockedUntil,
    inMarket = inMarket,
    offerAmount = offerAmount,
    status = status,
)
