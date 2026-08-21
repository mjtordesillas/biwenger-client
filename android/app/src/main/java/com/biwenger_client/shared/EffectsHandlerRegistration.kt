package com.biwenger_client.shared

import com.biwenger_client.core.effects.DispatchEvent
import com.biwenger_client.core.effects.DispatchEventHandler
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.core.navigation.NavigationEffect
import com.biwenger_client.core.navigation.NavigationEffectHandler
import com.biwenger_client.core.navigation.Navigator
import com.biwenger_client.core.state.Database
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.core.state.UpdateStateHandler
import com.biwenger_client.features.lineup.LineupEffectsHandlerRegistration
import com.biwenger_client.features.lineup.infrastructure.LineupService
import com.biwenger_client.features.market.MarketEffectsHandlerRegistration
import com.biwenger_client.features.market.infrastructure.MarketService

class EffectsHandlerRegistration(
    private val registry: Registry,
    private val database: Database,
    private val navigator: Navigator,
    private val lineupService: LineupService,
    private val marketService: MarketService,
) {
    fun register() {
        registry.registerEffectHandler(
            effectClass = DispatchEvent::class,
            handler = DispatchEventHandler(registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = UpdateState::class,
            handler = UpdateStateHandler(database)
        )
        registry.registerEffectHandler(
            effectClass = NavigationEffect::class,
            handler = NavigationEffectHandler(navigator = navigator)
        )
        LineupEffectsHandlerRegistration(
            registry = registry,
            lineupService = lineupService,
        ).register()
        MarketEffectsHandlerRegistration(registry = registry, marketService = marketService).register()
    }
}
