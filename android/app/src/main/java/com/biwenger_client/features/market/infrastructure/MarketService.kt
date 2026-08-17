package com.biwenger_client.features.market.infrastructure

import com.biwenger_client.domain.models.Player
import com.biwenger_client.infrastructure.network.Response

interface MarketService {
    suspend fun market(): Response<List<Player>>
}
