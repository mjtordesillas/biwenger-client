package com.biwenger_client.features.squad

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.squad.domain.coeffects.FetchMatchDayDetailsCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchMatchDayDetailsCoeffectHandler
import com.biwenger_client.features.squad.domain.coeffects.FetchPerformanceHistoryCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPerformanceHistoryCoeffectHandler
import com.biwenger_client.features.squad.domain.coeffects.FetchPriceHistoryCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchPriceHistoryCoeffectHandler
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffect
import com.biwenger_client.features.squad.domain.coeffects.FetchSquadCoeffectHandler
import com.biwenger_client.features.squad.infrastructure.MatchDayDetailsService
import com.biwenger_client.features.squad.infrastructure.PerformanceHistoryService
import com.biwenger_client.features.squad.infrastructure.PriceHistoryService
import com.biwenger_client.features.squad.infrastructure.SquadService

class SquadCoeffectsHandlerRegistration(
    private val registry: Registry,
    private val squadService: SquadService,
    private val priceHistoryService: PriceHistoryService,
    private val performanceHistoryService: PerformanceHistoryService,
    private val matchDayDetailsService: MatchDayDetailsService,
) {
    fun register() {
        registry.registerCoeffectHandler(
            coeffectClass = FetchSquadCoeffect::class,
            handler = FetchSquadCoeffectHandler(squadService = squadService)
        )
        registry.registerCoeffectHandler(
            coeffectClass = FetchPriceHistoryCoeffect::class,
            handler = FetchPriceHistoryCoeffectHandler(priceHistoryService = priceHistoryService)
        )
        registry.registerCoeffectHandler(
            coeffectClass = FetchPerformanceHistoryCoeffect::class,
            handler = FetchPerformanceHistoryCoeffectHandler(performanceHistoryService = performanceHistoryService)
        )
        registry.registerCoeffectHandler(
            coeffectClass = FetchMatchDayDetailsCoeffect::class,
            handler = FetchMatchDayDetailsCoeffectHandler(matchDayDetailsService = matchDayDetailsService)
        )
    }
}
