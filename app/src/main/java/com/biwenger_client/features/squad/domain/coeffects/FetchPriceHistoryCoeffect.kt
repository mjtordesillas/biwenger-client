package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.features.squad.infrastructure.PriceHistoryService
import com.biwenger_client.infrastructure.network.Response

// A data class, not an object like FetchSquadCoeffect — which player id
// to fetch varies per dispatch. See
// docs/adrs/ADR-009-event-parameterized-coeffects.md.
data class FetchPriceHistoryCoeffect(val playerId: Int) : Coeffect<PriceHistory>

class PriceHistoryFetchException(
    val response: Response.Error
) : Exception("Price history fetch failed: ${response.code}")

class FetchPriceHistoryCoeffectHandler(
    private val priceHistoryService: PriceHistoryService
) : CoeffectHandler<FetchPriceHistoryCoeffect, PriceHistory> {
    override suspend fun extract(coeffect: FetchPriceHistoryCoeffect): PriceHistory =
        when (val result = priceHistoryService.priceHistory(coeffect.playerId)) {
            is Response.Success -> result.body ?: throw PriceHistoryFetchException(
                response = Response.Error(code = 200, message = "empty body")
            )
            is Response.Error -> throw PriceHistoryFetchException(response = result)
        }
}
