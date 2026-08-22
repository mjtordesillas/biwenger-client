package com.biwenger_client.features.market

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.domain.effects.AcceptOfferEffect
import com.biwenger_client.features.market.domain.effects.AcceptOfferEffectHandler
import com.biwenger_client.features.market.domain.effects.CycleListingsEffect
import com.biwenger_client.features.market.domain.effects.CycleListingsEffectHandler
import com.biwenger_client.features.market.domain.effects.ListPlayerEffect
import com.biwenger_client.features.market.domain.effects.ListPlayerEffectHandler
import com.biwenger_client.features.market.domain.effects.PlaceBidEffect
import com.biwenger_client.features.market.domain.effects.PlaceBidEffectHandler
import com.biwenger_client.features.market.domain.effects.RejectOfferEffect
import com.biwenger_client.features.market.domain.effects.RejectOfferEffectHandler
import com.biwenger_client.features.market.domain.effects.RemoveBidEffect
import com.biwenger_client.features.market.domain.effects.RemoveBidEffectHandler
import com.biwenger_client.features.market.domain.effects.UnlistPlayerEffect
import com.biwenger_client.features.market.domain.effects.UnlistPlayerEffectHandler
import com.biwenger_client.features.market.infrastructure.MarketService

class MarketEffectsHandlerRegistration(
    private val registry: Registry,
    private val marketService: MarketService,
) {
    fun register() {
        registry.registerEffectHandler(
            effectClass = RejectOfferEffect::class,
            handler = RejectOfferEffectHandler(marketService = marketService, registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = AcceptOfferEffect::class,
            handler = AcceptOfferEffectHandler(marketService = marketService, registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = UnlistPlayerEffect::class,
            handler = UnlistPlayerEffectHandler(marketService = marketService, registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = ListPlayerEffect::class,
            handler = ListPlayerEffectHandler(marketService = marketService, registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = CycleListingsEffect::class,
            handler = CycleListingsEffectHandler(marketService = marketService, registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = RemoveBidEffect::class,
            handler = RemoveBidEffectHandler(marketService = marketService, registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = PlaceBidEffect::class,
            handler = PlaceBidEffectHandler(marketService = marketService, registry = registry)
        )
    }
}
