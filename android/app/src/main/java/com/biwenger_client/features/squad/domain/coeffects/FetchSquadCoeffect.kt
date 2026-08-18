package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.features.squad.domain.models.SquadPlayer
import com.biwenger_client.features.squad.infrastructure.SquadService
import com.biwenger_client.infrastructure.network.Response

object FetchSquadCoeffect : Coeffect<List<SquadPlayer>>

class SquadFetchException(
    val response: Response.Error
) : Exception("Squad fetch failed: ${response.code}")

class FetchSquadCoeffectHandler(
    private val squadService: SquadService
) : CoeffectHandler<FetchSquadCoeffect, List<SquadPlayer>> {
    override suspend fun extract(coeffect: FetchSquadCoeffect): List<SquadPlayer> =
        when (val result = squadService.squad()) {
            is Response.Success -> result.body ?: emptyList()
            is Response.Error -> throw SquadFetchException(response = result)
        }
}
