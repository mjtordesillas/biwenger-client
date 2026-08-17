package com.biwenger_client.helpers.builders

import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.features.squad.domain.models.PricePoint

fun aPricePoint(
    date: String = "2026-07-01",
    price: Long = 5500000,
) = PricePoint(date = date, price = price)

fun aPriceHistory(
    seasonStart: String = "2026-07-01",
    prices: List<PricePoint> = listOf(aPricePoint()),
) = PriceHistory(seasonStart = seasonStart, prices = prices)
