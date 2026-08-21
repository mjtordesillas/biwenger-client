package com.biwenger_client.features.market.infrastructure

import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerBid
import com.biwenger_client.features.market.domain.models.PlayerOffer
import com.biwenger_client.infrastructure.network.Response

interface MarketService {
    suspend fun market(): Response<List<MarketListing>>
    suspend fun myListings(): Response<List<MarketListing>>
    suspend fun offers(): Response<List<PlayerOffer>>
    suspend fun bids(): Response<List<PlayerBid>>
}
