package com.biwenger_client.features.market.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.infrastructure.MarketService

const val OFFER_ACCEPTANCE_FINISHED_EVENT = "market.offer-acceptance-finished"

data class AcceptOfferEffect(val offerId: Long) : Effect

class AcceptOfferEffectHandler(
    private val marketService: MarketService,
    private val registry: Registry,
) : EffectHandler<AcceptOfferEffect> {
    override suspend fun handle(effect: AcceptOfferEffect) {
        // Both outcomes refresh the server list; the dialog closes either way.
        try {
            marketService.acceptOffer(effect.offerId)
        } finally {
            registry.dispatch(event = event(name = OFFER_ACCEPTANCE_FINISHED_EVENT))
        }
    }
}
