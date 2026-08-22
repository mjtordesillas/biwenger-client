package com.biwenger_client.features.market.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.infrastructure.MarketService

const val CYCLE_LISTINGS_FINISHED_EVENT = "market.cycle-listings-finished"

// No payload — unlike UnlistPlayerEffect/ListPlayerEffect, the selection
// of which players to unlist/list now lives entirely server-side (see
// docs/backlog/done/cycle-player-listings.md and the
// backend's cycle-listings-api-handler.js); the client only knows this
// batch finished, not which ids it touched.
object CycleListingsEffect : Effect

class CycleListingsEffectHandler(
    private val marketService: MarketService,
    private val registry: Registry,
) : EffectHandler<CycleListingsEffect> {
    override suspend fun handle(effect: CycleListingsEffect) {
        try {
            marketService.cycleListings()
        } finally {
            registry.dispatch(event = event(name = CYCLE_LISTINGS_FINISHED_EVENT))
        }
    }
}
