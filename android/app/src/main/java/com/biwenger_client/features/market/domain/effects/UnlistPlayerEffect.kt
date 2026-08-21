package com.biwenger_client.features.market.domain.effects

import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.effects.EffectHandler
import com.biwenger_client.core.events.event
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.infrastructure.MarketService

const val UNLIST_PLAYER_FINISHED_EVENT = "market.unlist-player-finished"

data class UnlistPlayerEffect(val playerId: Int) : Effect

class UnlistPlayerEffectHandler(
    private val marketService: MarketService,
    private val registry: Registry,
) : EffectHandler<UnlistPlayerEffect> {
    override suspend fun handle(effect: UnlistPlayerEffect) {
        // Both outcomes clear the row's spinner and refresh the list.
        try {
            marketService.unlistPlayer(effect.playerId)
        } finally {
            registry.dispatch(event = event(name = UNLIST_PLAYER_FINISHED_EVENT, payload = effect.playerId))
        }
    }
}
