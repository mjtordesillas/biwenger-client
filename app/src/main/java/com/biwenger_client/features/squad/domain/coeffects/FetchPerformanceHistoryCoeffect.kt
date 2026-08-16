package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.infrastructure.PerformanceHistoryService
import com.biwenger_client.infrastructure.network.Response

data class FetchPerformanceHistoryCoeffect(val playerId: Int, val season: String) : Coeffect<PerformanceHistory>

class PerformanceHistoryFetchException(
    val response: Response.Error
) : Exception("Performance history fetch failed: ${response.code}")

class FetchPerformanceHistoryCoeffectHandler(
    private val performanceHistoryService: PerformanceHistoryService
) : CoeffectHandler<FetchPerformanceHistoryCoeffect, PerformanceHistory> {
    override suspend fun extract(coeffect: FetchPerformanceHistoryCoeffect): PerformanceHistory =
        when (val result = performanceHistoryService.performanceHistory(coeffect.playerId, coeffect.season)) {
            is Response.Success -> result.body ?: throw PerformanceHistoryFetchException(
                response = Response.Error(code = 200, message = "empty body")
            )
            is Response.Error -> throw PerformanceHistoryFetchException(response = result)
        }
}
