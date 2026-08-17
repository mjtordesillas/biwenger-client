package com.biwenger_client.helpers.builders

import com.biwenger_client.domain.models.Player

fun aPlayer(
    id: Int = 1,
    name: String = "Brugué",
    position: Int = 4,
    secondaryPosition: Int? = null,
    price: Long = 280000,
    priceIncrement: Long = 0,
    points: Int = 0,
    photoUrl: String = "https://cdn.biwenger.com/i/p/$id.png",
    teamCrestUrl: String = "https://cdn.biwenger.com/i/t/1.png",
) = Player(
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
