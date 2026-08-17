package com.biwenger_client.features.market

import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.StateInitializer
import com.biwenger_client.features.market.domain.models.MarketListing

class MarketStateInitializer : StateInitializer {
    override fun initialState(): Map<String, Any?> = mapOf(
        "market.players" to null as Loadable<List<MarketListing>>?,
    )
}
