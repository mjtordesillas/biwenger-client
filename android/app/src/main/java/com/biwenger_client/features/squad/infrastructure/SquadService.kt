package com.biwenger_client.features.squad.infrastructure

import com.biwenger_client.features.squad.domain.models.SquadPlayer
import com.biwenger_client.infrastructure.network.Response

interface SquadService {
    suspend fun squad(): Response<List<SquadPlayer>>
}
