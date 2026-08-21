package com.biwenger_client.features.market.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.infrastructure.MarketService

const val LIST_PLAYER_FINISHED_EVENT = "market.list-player-finished"

data class ListPlayerEffect(val playerId: Int) : Effect

class ListPlayerEffectHandler(
    private val marketService: MarketService,
    private val registry: Registry,
) : EffectHandler<ListPlayerEffect> {
    override suspend fun handle(effect: ListPlayerEffect) {
        // Both outcomes clear the card's spinner and refresh eligibility.
        try {
            marketService.listPlayer(effect.playerId)
        } finally {
            registry.dispatch(event = event(name = LIST_PLAYER_FINISHED_EVENT, payload = effect.playerId))
        }
    }
}
