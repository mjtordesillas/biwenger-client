package com.biwenger_client.features.market.domain.coeffects

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.infrastructure.MarketService
import com.biwenger_client.infrastructure.network.Response

object FetchMarketCoeffect : Coeffect<List<MarketListing>>

class MarketFetchException(
    val response: Response.Error
) : Exception("Market fetch failed: ${response.code}")

class FetchMarketCoeffectHandler(
    private val marketService: MarketService
) : CoeffectHandler<FetchMarketCoeffect, List<MarketListing>> {
    override suspend fun extract(coeffect: FetchMarketCoeffect): List<MarketListing> =
        when (val result = marketService.market()) {
            is Response.Success -> result.body ?: emptyList()
            is Response.Error -> throw MarketFetchException(response = result)
        }
}
