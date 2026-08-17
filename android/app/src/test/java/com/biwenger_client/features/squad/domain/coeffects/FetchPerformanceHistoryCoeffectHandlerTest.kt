package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.features.squad.infrastructure.PerformanceHistoryService
import com.biwenger_client.helpers.builders.aPerformanceHistory
import com.biwenger_client.infrastructure.network.Response
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FetchPerformanceHistoryCoeffectHandlerTest {

    private val performanceHistoryService = mock<PerformanceHistoryService>()
    private val handler = FetchPerformanceHistoryCoeffectHandler(performanceHistoryService = performanceHistoryService)

    @Test
    fun `extract returns the performance history for the coeffect's player id and season on success`() {
        runBlocking {
            val history = aPerformanceHistory()
            whenever(performanceHistoryService.performanceHistory(42, "previous")).thenReturn(Response.Success(history))

            val result = handler.extract(FetchPerformanceHistoryCoeffect(playerId = 42, season = "previous"))

            assertThat(result).isEqualTo(history)
        }
    }

    @Test
    fun `extract throws on error`() {
        runBlocking {
            whenever(performanceHistoryService.performanceHistory(42, "current")).thenReturn(Response.Error(502, "upstream_error"))

            assertThatThrownBy {
                runBlocking { handler.extract(FetchPerformanceHistoryCoeffect(playerId = 42, season = "current")) }
            }.isInstanceOf(PerformanceHistoryFetchException::class.java)
        }
    }
}
