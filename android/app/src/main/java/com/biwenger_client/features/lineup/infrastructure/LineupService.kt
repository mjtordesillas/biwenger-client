package com.biwenger_client.features.lineup.infrastructure

import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.infrastructure.network.Response

interface LineupService {
    suspend fun lineup(): Response<Lineup>
    suspend fun saveLineup(formation: String, playerIds: List<Int?>): Response<Lineup>
}
