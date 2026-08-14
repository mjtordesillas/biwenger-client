package com.biwenger_client.features.squad.domain.models

data class PricePoint(
    val date: String,
    val price: Long
)

data class PriceHistory(
    val seasonStart: String,
    val prices: List<PricePoint>
)
