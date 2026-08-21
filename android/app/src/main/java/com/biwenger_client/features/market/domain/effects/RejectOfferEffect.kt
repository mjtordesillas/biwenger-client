package com.biwenger_client.features.market.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.infrastructure.MarketService

const val OFFER_REJECTION_FINISHED_EVENT = "market.offer-rejection-finished"

data class RejectOfferEffect(val offerId: Long) : Effect

class RejectOfferEffectHandler(
    private val marketService: MarketService,
    private val registry: Registry,
) : EffectHandler<RejectOfferEffect> {
    override suspend fun handle(effect: RejectOfferEffect) {
        // Both outcomes refresh the server list; the dialog closes either way.
        try {
            marketService.rejectOffer(effect.offerId)
        } finally {
            registry.dispatch(event = event(name = OFFER_REJECTION_FINISHED_EVENT))
        }
    }
}
