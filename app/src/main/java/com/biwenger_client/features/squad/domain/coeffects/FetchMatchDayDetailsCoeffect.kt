package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.infrastructure.MatchDayDetailsService
import com.biwenger_client.infrastructure.network.Response

data class FetchMatchDayDetailsCoeffect(val playerId: Int, val matchDay: Int, val season: String) : Coeffect<MatchDayDetails>

class MatchDayDetailsFetchException(
    val response: Response.Error
) : Exception("Match day details fetch failed: ${response.code}")

class FetchMatchDayDetailsCoeffectHandler(
    private val matchDayDetailsService: MatchDayDetailsService
) : CoeffectHandler<FetchMatchDayDetailsCoeffect, MatchDayDetails> {
    override suspend fun extract(coeffect: FetchMatchDayDetailsCoeffect): MatchDayDetails =
        when (val result = matchDayDetailsService.matchDayDetails(coeffect.playerId, coeffect.matchDay, coeffect.season)) {
            is Response.Success -> result.body ?: throw MatchDayDetailsFetchException(
                response = Response.Error(code = 200, message = "empty body")
            )
            is Response.Error -> throw MatchDayDetailsFetchException(response = result)
        }
}
