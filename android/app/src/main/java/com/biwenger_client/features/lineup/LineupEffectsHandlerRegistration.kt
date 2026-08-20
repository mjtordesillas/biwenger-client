package com.biwenger_client.features.lineup

import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.features.lineup.domain.effects.SaveLineupEffect
import com.biwenger_client.features.lineup.domain.effects.SaveLineupEffectHandler
import com.biwenger_client.features.lineup.infrastructure.LineupService

// Feature-specific effects stay out of the shared EffectsHandlerRegistration
// (framework effects only) — mirrors LineupCoeffectsHandlerRegistration.
class LineupEffectsHandlerRegistration(
    private val registry: Registry,
    private val lineupService: LineupService,
) {
    fun register() {
        registry.registerEffectHandler(
            effectClass = SaveLineupEffect::class,
            handler = SaveLineupEffectHandler(lineupService = lineupService, registry = registry)
        )
    }
}
