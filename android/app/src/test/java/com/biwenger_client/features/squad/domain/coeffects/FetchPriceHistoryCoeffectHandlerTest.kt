package com.biwenger_client.features.squad.domain.coeffects

import com.biwenger_client.features.squad.infrastructure.PriceHistoryService
import com.biwenger_client.helpers.builders.aPriceHistory
import com.biwenger_client.infrastructure.network.Response
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FetchPriceHistoryCoeffectHandlerTest {

    private val priceHistoryService = mock<PriceHistoryService>()
    private val handler = FetchPriceHistoryCoeffectHandler(priceHistoryService = priceHistoryService)

    @Test
    fun `extract returns the price history for the coeffect's player id on success`() {
        runBlocking {
            val history = aPriceHistory()
            whenever(priceHistoryService.priceHistory(42)).thenReturn(Response.Success(history))

            val result = handler.extract(FetchPriceHistoryCoeffect(playerId = 42))

            assertThat(result).isEqualTo(history)
        }
    }

    @Test
    fun `extract throws on error`() {
        runBlocking {
            whenever(priceHistoryService.priceHistory(42)).thenReturn(Response.Error(502, "upstream_error"))

            assertThatThrownBy {
                runBlocking { handler.extract(FetchPriceHistoryCoeffect(playerId = 42)) }
            }.isInstanceOf(PriceHistoryFetchException::class.java)
        }
    }
}
