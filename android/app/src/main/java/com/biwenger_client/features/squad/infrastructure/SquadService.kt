package com.biwenger_client.features.squad.infrastructure

import com.biwenger_client.domain.models.Player
import com.biwenger_client.infrastructure.network.Response

interface SquadService {
    suspend fun squad(): Response<List<Player>>
}
