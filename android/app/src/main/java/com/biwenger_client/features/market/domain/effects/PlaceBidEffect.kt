package com.biwenger_client.features.market.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.infrastructure.MarketService

const val PLACE_BID_FINISHED_EVENT = "market.place-bid-finished"

data class PlaceBidEffect(val playerId: Int, val amount: Long) : Effect

class PlaceBidEffectHandler(
    private val marketService: MarketService,
    private val registry: Registry,
) : EffectHandler<PlaceBidEffect> {
    override suspend fun handle(effect: PlaceBidEffect) {
        // Both outcomes close the dialog; the bids list reload is what
        // shows the new bid on success — same reasoning as
        // RejectOfferEffectHandler.
        try {
            marketService.placeBid(effect.playerId, effect.amount)
        } finally {
            registry.dispatch(event = event(name = PLACE_BID_FINISHED_EVENT))
        }
    }
}
