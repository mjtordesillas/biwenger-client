package com.biwenger_client.features.market.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.infrastructure.MarketService

const val REMOVE_BID_FINISHED_EVENT = "market.remove-bid-finished"

data class RemoveBidEffect(val offerId: Long) : Effect

class RemoveBidEffectHandler(
    private val marketService: MarketService,
    private val registry: Registry,
) : EffectHandler<RemoveBidEffect> {
    override suspend fun handle(effect: RemoveBidEffect) {
        // Both outcomes clear the row's spinner and refresh the list —
        // same reasoning as UnlistPlayerEffectHandler.
        try {
            marketService.removeBid(effect.offerId)
        } finally {
            registry.dispatch(event = event(name = REMOVE_BID_FINISHED_EVENT, payload = effect.offerId))
        }
    }
}
