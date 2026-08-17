package com.biwenger_client.features.squad.infrastructure

import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.infrastructure.network.Response

interface PriceHistoryService {
    suspend fun priceHistory(playerId: Int): Response<PriceHistory>
}
