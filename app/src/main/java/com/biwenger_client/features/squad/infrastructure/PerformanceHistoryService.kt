package com.biwenger_client.features.squad.infrastructure

import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.infrastructure.network.Response

interface PerformanceHistoryService {
    suspend fun performanceHistory(playerId: Int): Response<PerformanceHistory>
}
