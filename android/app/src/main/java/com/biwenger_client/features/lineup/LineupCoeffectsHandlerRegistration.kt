package com.biwenger_client.features.lineup

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.lineup.domain.coeffects.FetchLineupCoeffect
import com.biwenger_client.features.lineup.domain.coeffects.FetchLineupCoeffectHandler
import com.biwenger_client.features.lineup.infrastructure.LineupService

class LineupCoeffectsHandlerRegistration(
    private val registry: Registry,
    private val lineupService: LineupService,
) {
    fun register() {
        registry.registerCoeffectHandler(
            coeffectClass = FetchLineupCoeffect::class,
            handler = FetchLineupCoeffectHandler(lineupService = lineupService)
        )
    }
}
