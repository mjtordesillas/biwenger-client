package com.biwenger_client.features.lineup.domain.coeffects

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.features.lineup.domain.models.Lineup
import com.biwenger_client.features.lineup.infrastructure.LineupService
import com.biwenger_client.infrastructure.network.Response

object FetchLineupCoeffect : Coeffect<Lineup>

class LineupFetchException(
    val response: Response.Error
) : Exception("Lineup fetch failed: ${response.code}")

class FetchLineupCoeffectHandler(
    private val lineupService: LineupService
) : CoeffectHandler<FetchLineupCoeffect, Lineup> {
    override suspend fun extract(coeffect: FetchLineupCoeffect): Lineup =
        when (val result = lineupService.lineup()) {
            is Response.Success -> result.body ?: Lineup(formation = "", players = emptyList(), credits = 0)
            is Response.Error -> throw LineupFetchException(response = result)
        }
}
