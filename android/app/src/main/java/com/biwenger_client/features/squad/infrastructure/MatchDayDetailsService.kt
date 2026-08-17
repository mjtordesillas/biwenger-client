package com.biwenger_client.features.squad.infrastructure

import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.infrastructure.network.Response

interface MatchDayDetailsService {
    suspend fun matchDayDetails(playerId: Int, matchDay: Int, season: String): Response<MatchDayDetails>
}
