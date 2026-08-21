package com.biwenger_client.helpers.builders

import com.biwenger_client.features.market.domain.models.PlayerOffer

fun aPlayerOffer(
    id: Int = 1,
    name: String = "Brugué",
    position: Int = 4,
    secondaryPosition: Int? = null,
    price: Long = 280000,
    priceIncrement: Long = 0,
    points: Int = 0,
    photoUrl: String = "https://cdn.biwenger.com/i/p/$id.png",
    teamCrestUrl: String = "https://cdn.biwenger.com/i/t/1.png",
    amount: Long = 300000,
    until: Long = 1787115600,
    bidder: String? = null,
) = PlayerOffer(
    id = id,
    name = name,
    position = position,
    secondaryPosition = secondaryPosition,
    price = price,
    priceIncrement = priceIncrement,
    points = points,
    photoUrl = photoUrl,
    teamCrestUrl = teamCrestUrl,
    amount = amount,
    until = until,
    bidder = bidder,
)
