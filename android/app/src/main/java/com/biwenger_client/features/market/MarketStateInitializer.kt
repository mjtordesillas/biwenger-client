package com.biwenger_client.features.market

import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.StateInitializer
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.PriceHistory

class MarketStateInitializer : StateInitializer {
    override fun initialState(): Map<String, Any?> = mapOf(
        "market.players" to null as Loadable<List<MarketListing>>?,
        "market.myListings" to null as Loadable<List<MarketListing>>?,
        "market.selectedPlayerId" to null as Int?,
        "market.priceHistory" to null as Loadable<PriceHistory>?,
        "market.performanceHistory" to null as Loadable<PerformanceHistory>?,
        "market.performanceHistorySeason" to "current",
        "market.selectedMatchDay" to null as Int?,
        "market.matchDayDetails" to null as Loadable<MatchDayDetails>?,
    )
}
