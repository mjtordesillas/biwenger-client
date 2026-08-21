package com.biwenger_client.features.market

import com.biwenger_client.core.state.Loadable
import com.biwenger_client.core.state.StateInitializer
import com.biwenger_client.features.market.domain.models.MarketListing
import com.biwenger_client.features.market.domain.models.PlayerBid
import com.biwenger_client.features.market.domain.models.PlayerOffer
import com.biwenger_client.features.squad.domain.models.MatchDayDetails
import com.biwenger_client.features.squad.domain.models.PerformanceHistory
import com.biwenger_client.features.squad.domain.models.PriceHistory
import com.biwenger_client.features.squad.domain.models.SquadPlayer

class MarketStateInitializer : StateInitializer {
    override fun initialState(): Map<String, Any?> = mapOf(
        "market.players" to null as Loadable<List<MarketListing>>?,
        "market.myListings" to null as Loadable<List<MarketListing>>?,
        "market.offers" to null as Loadable<List<PlayerOffer>>?,
        "market.bids" to null as Loadable<List<PlayerBid>>?,
        "market.offerToReject" to null as PlayerOffer?,
        "market.rejectingOffer" to false,
        "market.offerToAccept" to null as PlayerOffer?,
        "market.acceptingOffer" to false,
        "market.unlistingPlayerIds" to emptySet<Int>(),
        "market.listPlayerSquad" to null as Loadable<List<SquadPlayer>>?,
        "market.listingPlayerIds" to emptySet<Int>(),
        "market.selectedPlayerId" to null as Int?,
        "market.priceHistory" to null as Loadable<PriceHistory>?,
        "market.performanceHistory" to null as Loadable<PerformanceHistory>?,
        "market.performanceHistorySeason" to "current",
        "market.selectedMatchDay" to null as Int?,
        "market.matchDayDetails" to null as Loadable<MatchDayDetails>?,
    )
}
