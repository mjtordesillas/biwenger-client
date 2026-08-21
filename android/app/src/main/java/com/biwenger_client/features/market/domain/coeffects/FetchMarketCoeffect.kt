package com.biwenger_client.features.market.domain.coeffects

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.CoeffectHandler
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerOffer
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

// The requester's own listings — same shape as FetchMarketCoeffect, just
// against MarketService.myListings() instead of market(). Kept as its
// own coeffect type (not FetchMarketCoeffect again) since the two are
// loaded independently into separate state paths for the Current
// Market/My Listings subtabs — see MarketScreen.
object FetchMyMarketListingsCoeffect : Coeffect<List<MarketListing>>

class FetchMyMarketListingsCoeffectHandler(
    private val marketService: MarketService
) : CoeffectHandler<FetchMyMarketListingsCoeffect, List<MarketListing>> {
    override suspend fun extract(coeffect: FetchMyMarketListingsCoeffect): List<MarketListing> =
        when (val result = marketService.myListings()) {
            is Response.Success -> result.body ?: emptyList()
            is Response.Error -> throw MarketFetchException(response = result)
        }
}

// Standing offers on my squad players — same shape as the other two
// market coeffects, against MarketService.offers() and its own state
// path for the third (Offers) subtab.
object FetchOffersCoeffect : Coeffect<List<PlayerOffer>>

class FetchOffersCoeffectHandler(
    private val marketService: MarketService
) : CoeffectHandler<FetchOffersCoeffect, List<PlayerOffer>> {
    override suspend fun extract(coeffect: FetchOffersCoeffect): List<PlayerOffer> =
        when (val result = marketService.offers()) {
            is Response.Success -> result.body ?: emptyList()
            is Response.Error -> throw MarketFetchException(response = result)
        }
}
