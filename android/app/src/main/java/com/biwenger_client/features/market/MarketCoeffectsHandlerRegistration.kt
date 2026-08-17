package com.biwenger_client.features.market

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.market.domain.coeffects.FetchMarketCoeffect
import com.biwenger_client.features.market.domain.coeffects.FetchMarketCoeffectHandler
import com.biwenger_client.features.market.infrastructure.MarketService

class MarketCoeffectsHandlerRegistration(
    private val registry: Registry,
    private val marketService: MarketService,
) {
    fun register() {
        registry.registerCoeffectHandler(
            coeffectClass = FetchMarketCoeffect::class,
            handler = FetchMarketCoeffectHandler(marketService = marketService)
        )
    }
}
