package com.biwenger_client.features.market.domain.coeffects

import com.biwenger_client.features.market.infrastructure.MarketService
import com.biwenger_client.helpers.builders.aMarketListing
import com.biwenger_client.infrastructure.network.Response
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FetchMarketCoeffectHandlerTest {

    private val marketService = mock<MarketService>()
    private val handler = FetchMarketCoeffectHandler(marketService = marketService)

    @Test
    fun `extract returns the market listings on success`() {
        runBlocking {
            val listings = listOf(aMarketListing())
            whenever(marketService.market()).thenReturn(Response.Success(listings))

            val result = handler.extract(FetchMarketCoeffect)

            assertThat(result).isEqualTo(listings)
        }
    }

    @Test
    fun `extract throws on error`() {
        runBlocking {
            whenever(marketService.market()).thenReturn(Response.Error(502, "upstream_error"))

            assertThatThrownBy {
                runBlocking { handler.extract(FetchMarketCoeffect) }
            }.isInstanceOf(MarketFetchException::class.java)
        }
    }
}
