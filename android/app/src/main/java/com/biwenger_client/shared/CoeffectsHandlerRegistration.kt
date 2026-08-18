package com.biwenger_client.shared

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.lineup.LineupCoeffectsHandlerRegistration
import com.biwenger_client.features.lineup.infrastructure.LineupService
import com.biwenger_client.features.market.MarketCoeffectsHandlerRegistration
import com.biwenger_client.features.market.infrastructure.MarketService
import com.biwenger_client.features.squad.SquadCoeffectsHandlerRegistration
import com.biwenger_client.features.squad.infrastructure.MatchDayDetailsService
import com.biwenger_client.features.squad.infrastructure.PerformanceHistoryService
import com.biwenger_client.features.squad.infrastructure.PriceHistoryService
import com.biwenger_client.features.squad.infrastructure.SquadService

class CoeffectsHandlerRegistration(
    private val registry: Registry,
    private val squadService: SquadService,
    private val priceHistoryService: PriceHistoryService,
    private val performanceHistoryService: PerformanceHistoryService,
    private val matchDayDetailsService: MatchDayDetailsService,
    private val marketService: MarketService,
    private val lineupService: LineupService,
) {
    fun register() {
        SquadCoeffectsHandlerRegistration(
            registry = registry,
            squadService = squadService,
            priceHistoryService = priceHistoryService,
            performanceHistoryService = performanceHistoryService,
            matchDayDetailsService = matchDayDetailsService,
        ).register()
        MarketCoeffectsHandlerRegistration(
            registry = registry,
            marketService = marketService,
        ).register()
        LineupCoeffectsHandlerRegistration(
            registry = registry,
            lineupService = lineupService,
        ).register()
    }
}
